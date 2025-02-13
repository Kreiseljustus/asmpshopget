package io.github.kreiseljustus;

import java.util.Arrays;
import java.util.Objects;

public class ShopDataHolder {
    public String Owner;
    public int[] position;
    public float price;
    public String item;
    int action;                     // 0 is buying 1 is selling 2 is out of stock
    public int amount;
    public int dimension; //0 overworld, 1 nether, 2 end

    public String serverIp;

    public ShopDataHolder(String owner, int[] position, float price, String item, int action, int amount, int dimension, String ip) {
        this.Owner = owner;
        this.position = position;
        this.price = price;
        this.item = item;
        this.action = action;
        this.amount = amount;
        this.dimension = dimension;

        serverIp = ip;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ShopDataHolder that = (ShopDataHolder) obj;
        return Float.compare(that.price, price) == 0 &&
                action == that.action &&
                Objects.equals(Owner, that.Owner) &&
                Arrays.equals(position, that.position) &&
                Objects.equals(item, that.item) &&
                serverIp.equals(that.serverIp);
    }
}
