package il.ac.bgu.cs.bp.bpjsliveness;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;

import java.nio.file.Files;
import java.nio.file.Paths;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
public class RunMapper {
    public static void main(String[] args) throws Exception {
        String name = "sokoban";
        String map_name = args[0];
        StateSpaceMapper stateSpaceMapper = new StateSpaceMapper();
        final BProgram bprog = new ResourceBProgram(name+".js");
        String content = new String(Files.readAllBytes(Paths.get("sokoban_maps", map_name)));
        bprog.putInGlobalScope("MAP", content);
        MapperResult mapperResult = stateSpaceMapper.mapSpace(bprog);
        System.out.println(mapperResult);
        System.out.println("// Export to GraphViz...");
        String path = Paths.get("output", name + ".dot").toString();
        DotExporter dotExporter = new LivenessExporter(mapperResult, path, name);
        dotExporter.export();

    }
}
