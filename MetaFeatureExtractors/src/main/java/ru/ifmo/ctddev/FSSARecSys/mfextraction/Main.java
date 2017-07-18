package ru.ifmo.ctddev.FSSARecSys.mfextraction;

import ru.ifmo.ctddev.FSSARecSys.mfextraction.general.DataSetDimensionality;
import ru.ifmo.ctddev.FSSARecSys.mfextraction.general.NumberOfClasses;

public class Main {
    public static void main(String[] args) {
        System.out.println(new DataSetDimensionality().getName());
        System.out.println(new NumberOfClasses().getName());
    }
}
