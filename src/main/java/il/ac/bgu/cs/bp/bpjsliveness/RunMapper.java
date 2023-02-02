package il.ac.bgu.cs.bp.bpjsliveness;
import il.ac.bgu.cs.bp.bpjs.context.ContextBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.statespacemapper.GenerateAllTracesInspection;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperEdge;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperVertex;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.mozilla.javascript.NativeObject;

import static java.util.stream.Collectors.joining;

public class RunMapper {
    public static void main(String[] args) throws Exception {
        String name = "sokoban_cobp";
        String map_name = args[0];
        StateSpaceMapper stateSpaceMapper = new StateSpaceMapper();
        final BProgram bprog = new ContextBProgram(name+".js");
        String content = new String(Files.readAllBytes(Paths.get("sokoban_maps", map_name)));
        bprog.putInGlobalScope("MAP", content);
        long start = System.currentTimeMillis();
        MapperResult mapperResult = stateSpaceMapper.mapSpace(bprog);
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println("elapsedTime:");
        System.out.println(elapsedTime);
        System.out.println(mapperResult);
        System.out.println("// Export to GraphViz...");
        String path = Paths.get("output", name + "_" + args[0] + ".dot").toString();
        DotExporter dotExporter = new LivenessExporter(mapperResult, path, name);
        dotExporter.export();
        Map<String, Map<String, String>> map = transformGraph(mapperResult.graph);
        FileOutputStream myFileOutStream = new FileOutputStream(Paths.get("output", name + "_" + args[0] + ".ser").toString());
        ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);
        myObjectOutStream.writeObject(map);

    }

    public static String getVertexLabel(MapperVertex v) {
        return v.bpss.getBThreadSnapshots().stream()
                .filter(btss -> btss.getName().equals("data"))
                .map(btss -> {
                    return ((NativeObject)btss.getData()).get("str").toString();
                })
                .collect(joining(",", "", ""));
    }

    public static String getVertexHot(MapperVertex v) {
        return v.bpss.getBThreadSnapshots().stream()
                .filter(btss -> btss.getName().contains("Live copy: box"))
                .map(btss -> {
                    SyncStatement syst = btss.getSyncStatement();
                    return btss.getName().replaceFirst("Live copy: box ", "") + "," + (syst.isHot() ? "1": "0");
                })
                .collect(joining(",", "", ""));
    }

    public static Map<String, Map<String, String>> transformGraph(Graph<MapperVertex, MapperEdge> graph) {
        Map<String, Map<String, String>> map = new HashMap<>();
        String v1, v2;
        for (MapperVertex v : graph.vertexSet()){
            v1 = getVertexLabel(v);
            map.put(v1 , new HashMap<>());
            map.get(v1).put("H", getVertexHot(v));
        }
        for (MapperEdge e : graph.edgeSet()){
            v1 = getVertexLabel(graph.getEdgeSource(e));
            v2 = getVertexLabel(graph.getEdgeTarget(e));
            map.get(v1).put(e.event.toString(), v2);
        }
        return map;
    }



}
