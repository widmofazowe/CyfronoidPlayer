package eu.cyfronoid.audio.player.resources;

import java.awt.Dimension;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DimensionAdapter extends XmlAdapter<String, Dimension> {

    private static final String SEPARATOR = ":";

    @Override
    public String marshal(Dimension dimension) throws Exception {
        return dimension.getWidth() + SEPARATOR + dimension.getHeight();
    }

    @Override
    public Dimension unmarshal(String dimensionString) throws Exception {
        String[] dimension = dimensionString.split(SEPARATOR);
        return new Dimension(Integer.parseInt(dimension[0]), Integer.parseInt(dimension[1]));
    }

}