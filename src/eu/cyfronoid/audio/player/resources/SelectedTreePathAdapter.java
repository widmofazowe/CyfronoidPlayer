package eu.cyfronoid.audio.player.resources;

import javax.swing.tree.TreePath;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.base.Joiner;

public class SelectedTreePathAdapter extends XmlAdapter<String, TreePath> {
    private static final String SEPARATOR = "/";

    @Override
    public String marshal(TreePath treePath) throws Exception {
        return Joiner.on(SEPARATOR).join(treePath.getPath());
    }

    @Override
    public TreePath unmarshal(String path) throws Exception {
        return new TreePath(path.split(SEPARATOR));
    }

}
