package com.kafe.main;

import com.kafe.util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AppKasir extends JFrame {
    // --- Komponen Tab Kasir ---
    private JTable tabelKeranjang;
    private DefaultTableModel tableModelKeranjang;
    private JTextField txtTotal, txtBayar, txtKembalian, txtQty;
    private JComboBox<String> cbMenu;
    private double totalBelanja = 0;

    // --- Komponen Tab CRUD Menu ---
    private JTable tabelMenu;
    private DefaultTableModel tableModelMenu;
    private JTextField txtIdMenu, txtNamaMenu, txtHargaMenu;
    private JComboBox<String> cbJenisMenu;

    public AppKasir() {
        setTitle("Sistem Informasi POS & Manajemen Kafe");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Membuat sistem Tab
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Menu Kasir (Transaksi)", buatPanelKasir());
        tabbedPane.addTab("Kelola Menu (CRUD)", buatPanelCrudMenu());
        
        add(tabbedPane);

        // Load data awal saat aplikasi pertama kali jalan
        refreshDataKasir();
        loadDataTabelMenu();
    }

    // ==========================================
    // 1. BAGIAN UI & LOGIKA TAB KASIR
    // ==========================================
    private JPanel buatPanelKasir() {
        JPanel panelUtama = new JPanel(new BorderLayout(10, 10));
        panelUtama.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Input Atas
        JPanel panelInput = new JPanel(new GridLayout(3, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createTitledBorder("Pilih Pesanan"));
        
        panelInput.add(new JLabel(" Menu Kafe:"));
        cbMenu = new JComboBox<>();
        panelInput.add(cbMenu);

        panelInput.add(new JLabel(" Kuantitas (Qty):"));
        txtQty = new JTextField("1");
        panelInput.add(txtQty);

        JButton btnTambah = new JButton("Tambah ke Keranjang");
        panelInput.add(btnTambah);
        panelUtama.add(panelInput, BorderLayout.NORTH);

        // Panel Tabel Tengah
        String[] kolom = {"ID", "Nama Menu", "Harga", "Qty", "Subtotal"};
        tableModelKeranjang = new DefaultTableModel(kolom, 0);
        tabelKeranjang = new JTable(tableModelKeranjang);
        panelUtama.add(new JScrollPane(tabelKeranjang), BorderLayout.CENTER);

        // Panel Pembayaran Bawah
        JPanel panelBayar = new JPanel(new GridLayout(4, 2, 5, 5));
        panelBayar.setBorder(BorderFactory.createTitledBorder("Pembayaran"));

        panelBayar.add(new JLabel(" TOTAL BELANJA (Rp):"));
        txtTotal = new JTextField("0");
        txtTotal.setEditable(false);
        txtTotal.setFont(new Font("Arial", Font.BOLD, 14));
        panelBayar.add(txtTotal);

        panelBayar.add(new JLabel(" Jumlah Uang Bayar (Rp):"));
        txtBayar = new JTextField();
        panelBayar.add(txtBayar);

        JButton btnHitung = new JButton("Proses Transaksi");
        panelBayar.add(btnHitung);
        
        txtKembalian = new JTextField("0");
        txtKembalian.setEditable(false);
        panelBayar.add(txtKembalian);
        panelUtama.add(panelBayar, BorderLayout.SOUTH);

        // Event Listeners Kasir
        btnTambah.addActionListener(e -> tambahItemKasir());
        btnHitung.addActionListener(e -> prosesTransaksi());

        return panelUtama;
    }

    private void tambahItemKasir() {
        try {
            if (cbMenu.getSelectedItem() == null) return;
            
            String item = (String) cbMenu.getSelectedItem();
            String[] parts = item.split(" - ");
            String id = parts[0];
            String nama = parts[1];
            double harga = Double.parseDouble(parts[2].replace("Rp ", ""));
            int qty = Integer.parseInt(txtQty.getText());
            
            double subtotal = harga * qty;
            totalBelanja += subtotal;
            
            tableModelKeranjang.addRow(new Object[]{id, nama, harga, qty, subtotal});
            txtTotal.setText(String.valueOf(totalBelanja));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Input kuantitas tidak valid!");
        }
    }

    private void prosesTransaksi() {
        try {
            double uangBayar = Double.parseDouble(txtBayar.getText());
            if (uangBayar < totalBelanja) {
                JOptionPane.showMessageDialog(this, "Uang pembayaran kurang!");
                return;
            }
            double kembalian = uangBayar - totalBelanja;
            txtKembalian.setText(String.valueOf(kembalian));

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO tbl_transaksi (total_bayar, jumlah_uang, kembalian) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDouble(1, totalBelanja);
                ps.setDouble(2, uangBayar);
                ps.setDouble(3, kembalian);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Transaksi Berhasil!");
                tableModelKeranjang.setRowCount(0);
                totalBelanja = 0;
                txtTotal.setText("0");
                txtBayar.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal memproses transaksi: " + ex.getMessage());
        }
    }

    private void refreshDataKasir() {
        cbMenu.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_menu, nama_item, harga FROM tbl_menu";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbMenu.addItem(rs.getInt("id_menu") + " - " + rs.getString("nama_item") + " - Rp " + rs.getDouble("harga"));
            }
        } catch (Exception ex) {
            System.out.println("Error load menu kasir: " + ex.getMessage());
        }
    }

    // ==========================================
    // 2. BAGIAN UI & LOGIKA TAB CRUD MENU
    // ==========================================
    private JPanel buatPanelCrudMenu() {
        JPanel panelUtama = new JPanel(new BorderLayout(10, 10));
        panelUtama.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Input CRUD
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Form Data Menu"));
        
        panelForm.add(new JLabel(" ID Menu (Otomatis):"));
        txtIdMenu = new JTextField();
        txtIdMenu.setEditable(false);
        panelForm.add(txtIdMenu);

        panelForm.add(new JLabel(" Nama Menu:"));
        txtNamaMenu = new JTextField();
        panelForm.add(txtNamaMenu);

        panelForm.add(new JLabel(" Jenis:"));
        cbJenisMenu = new JComboBox<>(new String[]{"Makanan", "Minuman"});
        panelForm.add(cbJenisMenu);

        panelForm.add(new JLabel(" Harga (Rp):"));
        txtHargaMenu = new JTextField();
        panelForm.add(txtHargaMenu);
        panelUtama.add(panelForm, BorderLayout.NORTH);

        // Tabel Data Menu
        String[] kolom = {"ID", "Nama Menu", "Jenis", "Harga"};
        tableModelMenu = new DefaultTableModel(kolom, 0);
        tabelMenu = new JTable(tableModelMenu);
        panelUtama.add(new JScrollPane(tabelMenu), BorderLayout.CENTER);

        // Tombol Aksi CRUD
        JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnSimpan = new JButton("Simpan Baru");
        JButton btnUbah = new JButton("Ubah Data");
        JButton btnHapus = new JButton("Hapus Data");
        JButton btnReset = new JButton("Reset Form");
        
        panelAksi.add(btnSimpan);
        panelAksi.add(btnUbah);
        panelAksi.add(btnHapus);
        panelAksi.add(btnReset);
        panelUtama.add(panelAksi, BorderLayout.SOUTH);

        // Event Listeners CRUD
        tabelMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { klikTabelMenu(); }
        });
        btnSimpan.addActionListener(e -> simpanMenu());
        btnUbah.addActionListener(e -> ubahMenu());
        btnHapus.addActionListener(e -> hapusMenu());
        btnReset.addActionListener(e -> resetFormCrud());

        return panelUtama;
    }

    private void loadDataTabelMenu() {
        tableModelMenu.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM tbl_menu";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModelMenu.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_item"),
                    rs.getString("jenis"),
                    rs.getDouble("harga")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error load tabel menu: " + ex.getMessage());
        }
    }

    private void klikTabelMenu() {
        int baris = tabelMenu.getSelectedRow();
        if (baris != -1) {
            txtIdMenu.setText(tabelMenu.getValueAt(baris, 0).toString());
            txtNamaMenu.setText(tabelMenu.getValueAt(baris, 1).toString());
            cbJenisMenu.setSelectedItem(tabelMenu.getValueAt(baris, 2).toString());
            txtHargaMenu.setText(tabelMenu.getValueAt(baris, 3).toString());
        }
    }

    private void simpanMenu() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO tbl_menu (nama_item, jenis, harga) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNamaMenu.getText());
            ps.setString(2, cbJenisMenu.getSelectedItem().toString());
            ps.setDouble(3, Double.parseDouble(txtHargaMenu.getText()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Menu baru berhasil ditambahkan!");
            sinkronisasiData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan: " + ex.getMessage());
        }
    }

    private void ubahMenu() {
        if (txtIdMenu.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data di tabel dulu untuk diubah!");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE tbl_menu SET nama_item=?, jenis=?, harga=? WHERE id_menu=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtNamaMenu.getText());
            ps.setString(2, cbJenisMenu.getSelectedItem().toString());
            ps.setDouble(3, Double.parseDouble(txtHargaMenu.getText()));
            ps.setInt(4, Integer.parseInt(txtIdMenu.getText()));
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data menu berhasil diubah!");
            sinkronisasiData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengubah: " + ex.getMessage());
        }
    }

    private void hapusMenu() {
        if (txtIdMenu.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data di tabel dulu untuk dihapus!");
            return;
        }
        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus menu ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM tbl_menu WHERE id_menu=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(txtIdMenu.getText()));
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Data menu berhasil dihapus!");
                sinkronisasiData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage());
            }
        }
    }

    private void resetFormCrud() {
        txtIdMenu.setText("");
        txtNamaMenu.setText("");
        cbJenisMenu.setSelectedIndex(0);
        txtHargaMenu.setText("");
        tabelMenu.clearSelection();
    }

    private void sinkronisasiData() {
        // Method ini dipanggil setelah CRUD agar tabel & combobox kasir ter-update
        loadDataTabelMenu();
        refreshDataKasir();
        resetFormCrud();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AppKasir().setVisible(true);
        });
    }
}