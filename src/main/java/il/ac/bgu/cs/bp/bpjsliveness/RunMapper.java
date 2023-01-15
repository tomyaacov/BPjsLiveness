package il.ac.bgu.cs.bp.bpjsliveness;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.StateSpaceMapper;
import java.nio.file.Paths;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
public class RunMapper {
    public static void main(String[] args) throws Exception {
        String f = "sokoban";
        StateSpaceMapper stateSpaceMapper = new StateSpaceMapper();
        final BProgram bprog = new ResourceBProgram(f+".js");
        MapperResult mapperResult = stateSpaceMapper.mapSpace(bprog);
        System.out.println("// Export to GraphViz...");
        String path = Paths.get("output", f + ".dot").toString();
        DotExporter dotExporter = new DotExporter(mapperResult, path, f);
        dotExporter.export();

    }
}
