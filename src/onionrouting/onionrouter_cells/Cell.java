package onionrouting.onionrouter_cells;

import merrimackutil.json.JSONSerializable;

public abstract class Cell implements JSONSerializable{
    protected String circID;

    public String getCircID() {
        return circID;
    }
}
