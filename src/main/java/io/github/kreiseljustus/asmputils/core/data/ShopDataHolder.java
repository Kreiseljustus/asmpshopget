package io.github.kreiseljustus.asmputils.core.data;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class ShopDataHolder {
    public String Owner;
    public int[] position;
    public float price;
    public String item;

    public LocalDateTime updateTime;

    /**
     * Current State of the Shop
     * <ul>
     *     <li>{@code int} 0 is "buying"</li>
     *     <li>{@code int} 1 is "selling"</li>
     *     <li>{@code int} 2 is "out of stock"</li>
     * </ul>
     */
    public int action;
    public int amount;

    /**
     * Dimension in which the Shop is located
     * <ul>
     *     <li>{@code int} 0 is "overworld"</li>
     *     <li>{@code int} 1 is "nether"</li>
     *     <li>{@code int} 2 is "end"</li>
     * </ul>
     */
    public int dimension;

    public ShopDataHolder(@NotNull String owner, int[] position, float price, String item, int action, int amount, int dimension, LocalDateTime updateTime) throws ShopException {
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
        this.updateTime = updateTime;
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

    @Override
    public int hashCode() {
        int result = Objects.hash(Owner, Arrays.hashCode(position), price,item,action,amount,dimension);
        result = 31 * result + Arrays.hashCode(position);
        return result;
    }
}
