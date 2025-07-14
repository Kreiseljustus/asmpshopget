package io.github.kreiseljustus.asmpshopget;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class WaystoneDataHolder {
    public String Owner;
    public String Name;
    public int[] position;

    public WaystoneDataHolder(@NotNull String Owner, @NotNull String Name, int @NotNull [] position) {
        this.Owner = Owner;
        this.Name = Name;
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WaystoneDataHolder that = (WaystoneDataHolder) obj;
        return Objects.equals(that.Owner, Owner) &&
                Objects.equals(that.Name, Name) &&
                Arrays.equals(that.position, position);
    }
}
