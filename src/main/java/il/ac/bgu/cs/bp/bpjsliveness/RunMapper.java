package il.ac.bgu.cs.bp.bpjsliveness;
import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.statespacemapper.GenerateAllTracesInspection;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperEdge;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperVertex;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
public class RunMapper {
    public static void main(String[] args) throws Exception {
        String name = "sokoban_cobp";
        String map_name = args[0];
        StateSpaceMapper stateSpaceMapper = new StateSpaceMapper();
        final BProgram bprog = new ContextBProgram(name+".js");
        String content = new String(Files.readAllBytes(Paths.get("sokoban_maps", map_name)));
        bprog.putInGlobalScope("MAP", content);
        MapperResult mapperResult = stateSpaceMapper.mapSpace(bprog);
        System.out.println(mapperResult);
        System.out.println("// Export to GraphViz...");
        String path = Paths.get("output", name + "_" + args[0] + ".dot").toString();
//        MapperResult mapperResultTransformed = LivenessMapperResult.generate(transformGraph(mapperResult.graph));
        DotExporter dotExporter = new LivenessExporter(mapperResult, path, name);
        dotExporter.export();

    }

    public static Graph<MapperVertex, MapperEdge> transformGraph(Graph<MapperVertex, MapperEdge> graph) {
        DirectedPseudograph<MapperVertex, MapperEdge> newGraph = new DirectedPseudograph<>(MapperEdge.class);
        Map<MapperVertex, MapperVertex> d = new HashMap<>();
        for (MapperEdge e : graph.edgeSet()){
            if (e.event.toString().equals("Data")){
                if (d.containsKey(graph.getEdgeTarget(e))){
                    d.put(graph.getEdgeSource(e), d.get(graph.getEdgeTarget(e)));
                } else {
                    newGraph.addVertex(graph.getEdgeSource(e));
                    d.put(graph.getEdgeSource(e), graph.getEdgeSource(e));
                    d.put(graph.getEdgeTarget(e), graph.getEdgeSource(e));
                }
            }
        }
        for (MapperEdge e : graph.edgeSet()){
            if (!e.event.toString().equals("Data")){
                newGraph.addEdge(d.get(graph.getEdgeSource(e)), d.get(graph.getEdgeTarget(e)), e);
            }
        }

        return newGraph;
    }



}
