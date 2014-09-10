package eu.cyfronoid.audio.player.resources;

import java.awt.Point;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PointAdapter extends XmlAdapter<String, Point> {

    private static final String SEPARATOR = ":";

    @Override
    public String marshal(Point point) throws Exception {
        return point.getX() + SEPARATOR + point.getY();
    }

    @Override
    public Point unmarshal(String pointString) throws Exception {
        String[] point = pointString.split(SEPARATOR);
        return new Point(Integer.parseInt(point[0].replace(".0", "")), Integer.parseInt(point[1].replace(".0", "")));
    }

}