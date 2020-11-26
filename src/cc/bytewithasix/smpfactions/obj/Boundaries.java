package cc.bytewithasix.smpfactions.obj;

public class Boundaries {

    private int factionId;
    private Coords2D coordsOne;
    private Coords2D coordsTwo;

    public Boundaries(int factionId, Coords2D coordsOne, Coords2D coordsTwo) {
        this.factionId = factionId;
        this.coordsOne = coordsOne;
        this.coordsTwo = coordsTwo;
    }

    public int getFactionId() {
        return factionId;
    }

    public Coords2D getCoordsOne() {
        return coordsOne;
    }

    public Coords2D getCoordsTwo() {
        return coordsTwo;
    }
}
