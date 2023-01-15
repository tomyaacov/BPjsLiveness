package il.ac.bgu.cs.bp.bpjsliveness;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.statespacemapper.MapperResult;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperEdge;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.MapperVertex;
import il.ac.bgu.cs.bp.statespacemapper.jgrapht.exports.DotExporter;
import org.jgrapht.nio.BaseExporter;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static java.util.stream.Collectors.joining;
import org.mozilla.javascript.NativeObject;

public class LivenessExporter extends DotExporter {
    private Function<String, String> sanitizerProvider;
    public LivenessExporter(MapperResult res, String path, String runName) {
        super(res, path, runName);
        this.sanitizerProvider = sanitizerProvider();
    }

    protected Function<MapperEdge, Map<String, Attribute>> edgeAttributeProvider() {
        return e -> new HashMap<>(Map.of(
                "label", DefaultAttribute.createAttribute(sanitizerProvider.apply(e.event.toString()))
        ));
    }

    protected Function<MapperVertex, Map<String, Attribute>> vertexAttributeProvider() {
        return v -> new HashMap<>(Map.of(
                "id", DefaultAttribute.createAttribute(sanitizerProvider.apply(getID(v.bpss))),
                "hot", DefaultAttribute.createAttribute(sanitizerProvider.apply(getHot(v.bpss)))
        ));
    }

    protected String getHot(BProgramSyncSnapshot bpss) {
        return bpss.getBThreadSnapshots().stream()
                .map(btss -> {
                    SyncStatement syst = btss.getSyncStatement();
                    return btss.getName() + "," + (syst.isHot() ? "1": "0");
                })
                .collect(joining(",\n", "", ""));
    }

    protected String getID(BProgramSyncSnapshot bpss) {
        return bpss.getBThreadSnapshots().stream()
                .filter(btss -> btss.getName().equals("data"))
                .map(btss -> {
                    return ((NativeObject)btss.getData()).get("str").toString();
                })
                .collect(joining(",\n", "", ""));
    }

}
