package com.kafe.model;

public class Minuman extends MenuItem {
    private String ukuranGelas;

    public Minuman(int id, String nama, double harga, String ukuranGelas) {
        super(id, nama, harga);
        this.ukuranGelas = ukuranGelas;
    }

    @Override
    public double hitungHargaFinal(int kuantitas) {
        // Logika Polimorfisme: Jika ukuran Large, tambah Rp 3000
        double tambahan = ukuranGelas.equalsIgnoreCase("Large") ? 3000 : 0;
        return (this.getHarga() + tambahan) * kuantitas;
    }
}