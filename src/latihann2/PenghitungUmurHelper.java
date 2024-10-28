package latihann2;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.function.Supplier;

public class PenghitungUmurHelper {

    // Metode untuk menghitung umur
    public String hitungUmurDetail(LocalDate lahir, LocalDate sekarang) {
        int tahun = sekarang.getYear() - lahir.getYear();
        int bulan = sekarang.getMonthValue() - lahir.getMonthValue();
        int hari = sekarang.getDayOfMonth() - lahir.getDayOfMonth();

        if (hari < 0) {
            bulan--;
            hari += bulan < 0 ? 30 : getDaysInMonth(lahir.getMonthValue());
        }

        if (bulan < 0) {
            tahun--;
            bulan += 12;
        }

        return String.format("%d tahun, %d bulan, %d hari", tahun, bulan, hari);
    }

    // Metode untuk menghitung ulang tahun berikutnya
    public LocalDate hariUlangTahunBerikutnya(LocalDate lahir, LocalDate sekarang) {
        LocalDate nextBirthday = lahir.withYear(sekarang.getYear());
        if (nextBirthday.isBefore(sekarang) || nextBirthday.isEqual(sekarang)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        return nextBirthday;
    }

    // Metode untuk mendapatkan hari dalam bahasa Indonesia
    public String getDayOfWeekInIndonesian(LocalDate date) {
        String[] hariDalamBahasaIndonesia = {"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        return hariDalamBahasaIndonesia[date.getDayOfWeek().getValue() % 7];
    }

    // Metode untuk mengambil peristiwa penting untuk tanggal tertentu
    public void getPeristiwaBarisPerBaris(LocalDate tanggal, JTextArea txtAreaPeristiwa, Supplier<Boolean> shouldStop) {
        try {
            String urlString = "https://byabbe.se/on-this-day/" + tanggal.getMonthValue() + "/" + tanggal.getDayOfMonth() + "/events.json";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Kode respons HTTP: " + responseCode);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(content.toString());
            JSONArray events = json.getJSONArray("events");
            if (events.length() == 0) {
                javax.swing.SwingUtilities.invokeLater(() -> txtAreaPeristiwa.setText("Tidak ada peristiwa penting yang ditemukan pada tanggal ini."));
            } else {
                for (int i = 0; i < events.length(); i++) {
                    JSONObject event = events.getJSONObject(i);
                    String year = event.getString("year");
                    String description = event.getString("description");
                    String translatedDescription = translateToIndonesian(description); // Panggil metode terjemahan yang diperbarui
                    String peristiwa = year + ": " + translatedDescription;

                    // Tambahkan peristiwa ke JTextArea di Event Dispatch Thread
                    javax.swing.SwingUtilities.invokeLater(() -> txtAreaPeristiwa.append(peristiwa + "\n"));
                }
            }
        } catch (Exception e) {
            javax.swing.SwingUtilities.invokeLater(() -> txtAreaPeristiwa.setText("Gagal mendapatkan data peristiwa: " + e.getMessage()));
        }
    }

    // Menerjemahkan teks ke bahasa Indonesia
    private String translateToIndonesian(String text) {
        try {
            String urlString = "https://lingva.ml/api/v1/en/id/" + text.replace(" ", "%20");
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Kode respons HTTP: " + responseCode);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();
            JSONObject json = new JSONObject(content.toString());
            return json.getString("translation");
        } catch (Exception e) {
            return text + " (Gagal diterjemahkan)";
        }
    }

    // Metode bantu untuk mendapatkan jumlah hari dalam sebulan
    private int getDaysInMonth(int month) {
        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                return 31;
            case 4: case 6: case 9: case 11:
                return 30;
            case 2:
                return 28; // Tidak menangani tahun kabisat untuk kesederhanaan
            default:
                return 0;
        }
    }
}