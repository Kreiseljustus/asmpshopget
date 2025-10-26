package io.github.kreiseljustus.asmputils.core.data;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class WaystoneDataHolder {
    public String Owner;
    public String Name;
    public int[] position;

    public int dimension; //0 overworld, 1 nether, 2 end

    public WaystoneDataHolder(@NotNull String Owner, @NotNull String Name, int @NotNull [] position, int dimension) {
        this.Owner = Owner;
        this.Name = Name;
        this.position = position;

        this.dimension = dimension;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WaystoneDataHolder that = (WaystoneDataHolder) obj;
        return Objects.equals(that.Owner, Owner) &&
                Objects.equals(that.Name, Name) &&
                Objects.equals(that.dimension, dimension) &&
                Arrays.equals(that.position, position);
    }
}
