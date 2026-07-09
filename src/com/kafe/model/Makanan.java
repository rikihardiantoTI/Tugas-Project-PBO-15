package com.kafe.model;

public class Makanan extends MenuItem {
    public Makanan(int id, String nama, double harga) {
        super(id, nama, harga);
    }

    @Override
    public double hitungHargaFinal(int kuantitas) {
        return this.getHarga() * kuantitas;
    }
}