# 📁 Folder API - Sistem Kenaikan Jilid TPQ

Folder ini berisi file-file PHP backend untuk sistem kenaikan jilid TPQ.

## 📋 Daftar File

### 1. `db_config.php` ⚙️
**File konfigurasi database**
- **PENTING:** Edit file ini dan ganti kredensial database Anda
```php
$host = '127.0.0.1';
$dbname = 'u221955505_db_posylike';
$username = 'u221955505_posylike';  // GANTI INI
$password = 'your_password_here';    // GANTI INI
```

### 2. `database_schema.sql` 🗄️
**Schema database lengkap**
- Berisi CREATE TABLE untuk `jilid`, `siswa_jilid`, `kenaikan_jilid`
- Berisi INSERT data 7 jilid (Jilid 1-6 + Al-Qur'an)
- **Cara pakai:** Import file ini ke database MySQL Anda via phpMyAdmin

### 3. `get_all_jilid.php` 📖
**Endpoint: GET semua jilid**
- URL: `https://kampunginggrisori.com/api/get_all_jilid.php`
- Method: GET
- Response: List semua jilid (1-7)

### 4. `get_siswa_jilid.php` 👤
**Endpoint: Cek jilid siswa saat ini**
- URL: `https://kampunginggrisori.com/api/get_siswa_jilid.php?no_induk=XXX`
- Method: GET
- Parameter: `no_induk` (required)
- Response: Data jilid siswa sekarang
- **Auto-create:** Jika siswa baru, otomatis dimasukkan ke Jilid 1

### 5. `save_kenaikan_jilid.php` ⬆️
**Endpoint: Simpan kenaikan jilid**
- URL: `https://kampunginggrisori.com/api/save_kenaikan_jilid.php`
- Method: POST
- Body (JSON):
```json
{
  "no_induk": "SISWA001",
  "id_jilid_lama": 1,
  "id_jilid_baru": 2,
  "id_ujian": 10,
  "nilai_ujian": 85.0,
  "persentase_ujian": 85.0,
  "tanggal_naik": "2025-12-26"
}
```

### 6. `save_ujian.php` 💾
**Endpoint: Simpan data ujian (UPDATED)**
- URL: `https://kampunginggrisori.com/api/save_ujian.php`
- Method: POST
- Body (JSON):
```json
{
  "no_induk": "SISWA001",
  "id_jilid": 1,
  "nilai_total": 85.0,
  "tanggal_ujian": "2025-12-26"
}
```
- **PENTING:** Versi baru ini mengembalikan `id_ujian` yang diperlukan untuk tracking kenaikan jilid

### 7. `update_nama_siswa.php` ✏️
**Endpoint: Update nama siswa**
- URL: `https://kampunginggrisori.com/api/update_nama_siswa.php`
- Method: POST
- Body (JSON):
```json
{
  "no_induk": "SISWA001",
  "nama_siswa": "Ahmad Zaki"
}
```

## 🚀 Cara Deploy

### Step 1: Setup Database
1. Buka phpMyAdmin
2. Pilih database: `u221955505_db_posylike`
3. Klik tab **Import**
4. Upload file `database_schema.sql`
5. Klik **Go/Execute**

### Step 2: Konfigurasi Database
1. Edit file `db_config.php`
2. Ganti `$username` dan `$password` dengan kredensial database Anda
3. Simpan file

### Step 3: Upload ke Server
1. Upload **SEMUA file** di folder ini ke server Anda
2. Path tujuan: `https://kampunginggrisori.com/api/`
3. Pastikan file `db_config.php` berada di folder yang sama dengan file PHP lainnya

### Step 4: Set Permissions
Pastikan semua file PHP memiliki permission **644**:
```bash
chmod 644 *.php
```

## ✅ Testing Endpoints

### Test 1: Get All Jilid
```bash
curl https://kampunginggrisori.com/api/get_all_jilid.php
```
Expected: JSON dengan 7 jilid

### Test 2: Get Siswa Jilid
```bash
curl "https://kampunginggrisori.com/api/get_siswa_jilid.php?no_induk=TEST001"
```
Expected: Siswa baru akan auto-create di Jilid 1

### Test 3: Save Ujian
```bash
curl -X POST https://kampunginggrisori.com/api/save_ujian.php \
  -H "Content-Type: application/json" \
  -d '{"no_induk":"TEST001","id_jilid":1,"nilai_total":85,"tanggal_ujian":"2025-12-26"}'
```
Expected: Response dengan `id_ujian`

## 🔧 Troubleshooting

### Error: "Connection failed"
- Periksa kredensial di `db_config.php`
- Pastikan database server running

### Error: "Table doesn't exist"
- Import file `database_schema.sql` terlebih dahulu

### Error: "Access denied"
- Periksa username dan password di `db_config.php`
- Pastikan user database memiliki privilege: SELECT, INSERT, UPDATE, DELETE

## 📊 Database Tables

Setelah import `database_schema.sql`, Anda akan memiliki:

### Table: `jilid`
- `id_jilid` (1-7)
- `nama_jilid` (Jilid 1, Jilid 2, ..., Al-Qur'an)
- `urutan` (1-7)
- `passing_grade` (default: 70.00%)

### Table: `siswa_jilid`
- `no_induk` (unique)
- `nama_siswa`
- `id_jilid_sekarang` (1-7)
- `tanggal_mulai`

### Table: `kenaikan_jilid`
- `id_kenaikan`
- `no_induk`
- `id_jilid_lama`
- `id_jilid_baru`
- `id_ujian`
- `nilai_ujian`
- `persentase_ujian`
- `tanggal_naik`

### Table: `ujian`
- `id_ujian`
- `no_induk`
- `id_jilid`
- `nilai_total`
- `persentase`
- `status`
- `Tanggal_ujian`

## 🎯 Flow Sistem

1. Siswa mulai ujian → `get_siswa_jilid.php` (cek jilid sekarang)
2. Siswa selesai ujian → `save_ujian.php` (dapat id_ujian)
3. Jika nilai ≥ 70% → `save_kenaikan_jilid.php` (naik jilid)
4. Update `siswa_jilid` table (jilid baru)
5. Simpan riwayat di `kenaikan_jilid` table

## 📞 Support

Jika ada masalah, cek:
1. Log error di server (biasanya di `/logs/` atau `error_log`)
2. Response JSON dari setiap endpoint
3. Android Logcat dengan tag `MySQLApiService`
