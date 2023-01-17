importPackage(Packages.events);

MAP = MAP.split("\n")

function box_in_target(box, targets) {
    for (let i = 0; i < targets.length; i++) {
        if (targets[i].length === box.length && targets[i].every(function(value, index) { return value === box[index]})){
            return true;
        }
    }
    return false;
}

function data_to_str(player_location, boxes) {
    let box_list = [];
    for (let b = 0; b < boxes.length; b++) {
        box_list.push(boxes[b].location)
    }
    let s = player_location[0].toString() + "_" + player_location[1].toString() + "_" + "D";
    for (let i = 0; i < box_list.length; i++){
        s = s + "_" + box_list[i][0].toString() + "_" + box_list[i][1].toString()
    }
    return s;
}

function action_to_new_location(action, i, j) {
    if (action === "Up") {
        return [i - 1, j];
    } else if (action === "Down") {
        return [i + 1, j];
    } else if (action === "Left") {
        return [i, j - 1];
    } else if (action === "Right") {
        return [i, j + 1];
    }
}

function event_to_new_location(dir, i, j) {
    return action_to_new_location(dir, i, j);
}

function event_to_2_steps_trajectory(event, i, j) {
    let [new_i, new_j] = event_to_new_location(event, i, j);
    return event_to_new_location(event, new_i, new_j);
}



function is_adjacent(l1, l2) {
    let terms = [];
    terms.push(l1[0] === l2[0] && l1[1] === l2[1] + 1);
    terms.push(l1[0] === l2[0] && l1[1] === l2[1] - 1);
    terms.push(l1[0] === l2[0] + 1 && l1[1] === l2[1]);
    terms.push(l1[0] === l2[0] - 1 && l1[1] === l2[1]);
    return terms.reduce((a, b) => a + b, 0) === 1;
}

function find_adjacent_objects(list1, list2) {
    return list1.flatMap(l1 =>
        list2.map(l2 =>
            is_adjacent(l1, l2) ? [l1, l2] : []
        )
    ).filter(x => x.length > 0);
}

function find_adjacent_boxes(location, l) {
    return l.filter(l2 => is_adjacent(location, l2)).map(x => [location, x])
}




function get_action(i, j, next_i, next_j){
    if (i === next_i + 1) {
        return ["Up", i + 1, j];
    } else if (i === next_i - 1) {
        return ["Down", i - 1, j];
    } else if (j === next_j + 1) {
        return ["Left", i, j + 1];
    } else if (j === next_j - 1) {
        return ["Right", i, j - 1];
    }
}

function block_action(neighbors_list, i, j) {
    let current_list = []
    for (let k = 0; k < neighbors_list.length; k++) {
        let a = get_action(neighbors_list[k][0][0], neighbors_list[k][0][1], neighbors_list[k][1][0], neighbors_list[k][1][1]);
        if (a[1] === i && a[2] === j){
            current_list.push(Action(a[0]));
        }
    }
    return current_list;
}

let dataEventSet = bp.EventSet( "dataEventSet", function(evt){
    return evt.name === "Data";
});

let walls_list = [];
let box_list = [];
let target_list = [];
let player_location = [];
let boxIndex=0
for (let i = 0; i < MAP.length; i++) {
    for (let j = 0; j < MAP[i].length; j++) {
        if (MAP[i][j] === "a"){
            player_location.push(i);
            player_location.push(j);
            ctx.populateContext([
                ctx.Entity('player','player',{location:player_location}),
            ])
        }
        if (MAP[i][j] === "X"){
            walls_list.push([i,j])
        }
        if (MAP[i][j] === "b"){
            box_list.push([i,j])
            ctx.populateContext([
                ctx.Entity('box'+boxIndex,'box',{location:[i,j], index:boxIndex}),
            ])
            boxIndex++
        }
        if (MAP[i][j] === "t"){
            target_list.push([i,j])
        }
    }
}

ctx.registerQuery('Player.Any', function(e){
    return e.type=='player'
})

ctx.registerQuery('Box.Any', function(e){
    return e.type=='box'
})

ctx.registerEffect('Action', function (data) {
    let player = ctx.getEntityById('player')
    player.location = event_to_new_location(data,player.location[0],player.location[1])

    let boxes = ctx.runQuery('Box.Any')
    for (let b = 0; b < boxes.length; b++) {
        if ((player.location[0] === boxes[b].location[0]) &&
            (player.location[1] === boxes[b].location[1])) {
            boxes[b].location = event_to_new_location(data,player.location[0],player.location[1])
        }
    }
})

bthread( "player", function(){
    while (true){
        // sync( {waitFor:dataEventSet} );
        sync( {request:[Action("Up"), Action("Down"), Action("Left"), Action("Right")]} );
    }
} );

ctx.bthread( "wall", 'Player.Any', function(p){
    let e = null;
    let player_location = null;
    let neighbors_list = null;
    let block_list = null;
    while (true){
        // e = sync( {waitFor:dataEventSet} );
        player_location = p.location
        neighbors_list = find_adjacent_boxes(player_location, walls_list)
        block_list = [];
        for (let i = 0; i < neighbors_list.length; i++) {
            block_list.push(Action(get_action(neighbors_list[i][0][0], neighbors_list[i][0][1], neighbors_list[i][1][0], neighbors_list[i][1][1] )[0]))
        }
        e = null;
        player_location = null;
        neighbors_list = null;
        sync( {block: block_list, waitFor:bp.eventSets.all} );
    }
} );

// for (let i = 0; i < box_list.length; i++) {
    ctx.bthread( "box", 'Box.Any', function(b){
        let first_time = true;
        let e = null;
        let double_object_movement = null;
        let box_list = null;
        let p = ctx.getEntityById('player');
        let boxes = ctx.runQuery('Box.Any');
        while (true){
            // e = sync( {waitFor:dataEventSet} );
            box_list = [];
            for (let b = 0; b < boxes.length; b++) {
                box_list.push(boxes[b].location)
            }
            let neighbors_list = find_adjacent_boxes(b.location, walls_list).concat(
                find_adjacent_boxes(b.location, box_list));
            double_object_movement =  block_action(neighbors_list, p.location[0], p.location[1])
            box_list = null;
            let in_target = box_in_target(b.location, target_list)
            e = null;
            if ((!in_target) && (!first_time)){
                sync( {block:double_object_movement, waitFor:bp.eventSets.all} ,null,true);
            } else {
                sync( {block:double_object_movement, waitFor:bp.eventSets.all} );
            }
            first_time = false
        }
    });
// }
bthread( "data", {str: "I"+data_to_str(ctx.getEntityById('player').location, ctx.runQuery('Box.Any'))}, function() {
    while (true) {
        sync({waitFor: bp.eventSets.all});
        bp.thread.data.str = "S"+data_to_str(ctx.getEntityById('player').location, ctx.runQuery('Box.Any'));

    }
});
