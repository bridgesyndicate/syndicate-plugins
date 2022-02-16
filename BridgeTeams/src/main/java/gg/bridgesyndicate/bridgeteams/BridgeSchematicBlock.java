package gg.bridgesyndicate.bridgeteams;


class BridgeSchematicBlock {
    public int x;
    public int y;
    public int z;
    public int id;
    public int data;

    @SuppressWarnings("unused") //used via jackson deserialization
    public BridgeSchematicBlock() { }

    @SuppressWarnings("unused") //used via jackson deserialization
    public BridgeSchematicBlock(int x, int y, int z, int id, int data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.data = data;
    }
}
