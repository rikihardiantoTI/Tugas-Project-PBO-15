package com.kafe.model;

public abstract class MenuItem {
    private int id;
    private String nama;
    private double harga;

    public MenuItem(int id, String nama, double harga) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }

    // Metode abstrak untuk di-override (Polimorfisme)
    public abstract double hitungHargaFinal(int kuantitas);
}