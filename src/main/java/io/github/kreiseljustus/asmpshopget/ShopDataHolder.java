package io.github.kreiseljustus.asmpshopget;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

class ShopException extends Exception {
    ShopException() {
        super("Failed to create shop data");
    }
}

public class ShopDataHolder {
    public String Owner;
    public int[] position;
    public float price;
    public String item;
    int action; // 0 is buying 1 is selling 2 is out of stock
    public int amount;
    public int dimension; //0 overworld, 1 nether, 2 end

    public ShopDataHolder(@NotNull String owner, int[] position, float price, String item, int action, int amount, int dimension) throws ShopException {
        if(owner.isEmpty()) throw new ShopException();
        this.Owner = owner;
        if(position.length < 3) throw new ShopException();
        this.position = position;
        if(price < 0 || price > Float.MAX_VALUE - 1) throw new ShopException();
        this.price = price;
        if(item.isEmpty()) throw new ShopException();
        this.item = item;
        if(action < 0 || action > 2) throw new ShopException();
        this.action = action;
        if(amount < 0) throw new ShopException();
        this.amount = amount;
        if(dimension < 0 || dimension > 2) throw new ShopException();
        this.dimension = dimension;
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
                Objects.equals(item, that.item);
    }
}
