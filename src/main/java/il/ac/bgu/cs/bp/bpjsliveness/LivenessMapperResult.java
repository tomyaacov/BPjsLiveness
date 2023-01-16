package il.ac.bgu.cs.bp.bpjsliveness;

import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperEdge;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperVertex;
import org.jgrapht.Graph;

public class LivenessMapperResult extends MapperResult {
    protected LivenessMapperResult(Graph<MapperVertex, MapperEdge> graph) {
        super(graph);
    }

    public static LivenessMapperResult generate(Graph<MapperVertex, MapperEdge> graph) {
        return new LivenessMapperResult(graph);
    }
}
