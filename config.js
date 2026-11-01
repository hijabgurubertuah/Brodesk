/**
 * Konfigurasi Halaman Unduh BroDesk
 * Pengembang dapat memperbarui data aplikasi di berkas ini untuk merilis versi baru.
 */
const BroDeskConfig = {
  appName: "BroDesk",
  developerName: "Guber Smart",
  version: "1.0.0",
  updatedDate: "1 November 2024",
  fileSize: "23 MB",
  minAndroid: "7.0+",
  packageId: "com.gubersmart.brodesk",
  apkFileName: "BroDesk.apk",
  category: "Alat",
  ageRating: "3+",
  containsAds: false,
  inAppPurchases: false,
  aboutShort: "BroDesk adalah browser Android yang menampilkan situs seperti di komputer, dengan kursor virtual, tekan lama sebagai klik kanan, banyak tab, bookmark, dan unduhan.",
  aboutFull: "BroDesk dirancang khusus bagi Anda yang sering mengakses situs berantarmuka desktop di layar smartphone Android. Dilengkapi sistem touchpad virtual presisi, Anda dapat menggerakkan kursor, mengklik tautan kecil, memilih teks, menjalankan klik kanan lewat tekan lama, dan menggunakan tombol pintasan keyboard praktis (Ctrl+A, Ctrl+C, Ctrl+V, Delete, serta Zoom In/Out). Seluruh data jelajah, bookmark, dan riwayat disimpan secara lokal dan aman di perangkat Anda.",
  categoryChips: [
    "Browser",
    "Mode Desktop",
    "Kursor Virtual",
    "Tanpa Iklan"
  ],
  features: [
    {
      title: "Mode Desktop",
      desc: "Tampilkan situs web dalam tata letak komputer desktop penuh",
      icon: "monitor"
    },
    {
      title: "Kursor Virtual",
      desc: "Touchpad sentuh dengan navigasi pointer yang presisi",
      icon: "mouse-pointer"
    },
    {
      title: "Klik Kanan Instan",
      desc: "Tekan lama pada kursor untuk memunculkan menu konteks situs",
      icon: "hand"
    },
    {
      title: "Banyak Tab",
      desc: "Buka dan beralih antar situs web favorit dengan cepat",
      icon: "layers"
    },
    {
      title: "Bookmark & Riwayat",
      desc: "Simpan alamat favorit dan telusuri jejak jelajah Anda",
      icon: "bookmark"
    },
    {
      title: "Pintasan Cepat",
      desc: "Tombol Pilih Semua, Salin, Tempel, Hapus, dan Zoom In/Out",
      icon: "sliders"
    }
  ],
  whatsNew: [
    "Navigasi kursor virtual presisi dengan touchpad sentuh interaktif",
    "Tombol pintasan cepat: Pilih Semua (Ctrl+A), Salin (Ctrl+C), Tempel (Ctrl+V), dan Hapus",
    "Tombol pembesaran halaman instan (Zoom In & Zoom Out)",
    "Kunci layar (Screen Lock) agar kursor bebas digerakkan tanpa menggeser halaman",
    "Klik kanan instan lewat tahan lama (long-press)",
    "Pencarian terintegrasi dan bilah alamat luas yang nyaman disentuh"
  ],
  dataSafety: [
    "Data disimpan di HP Anda tanpa pengumpulan informasi pribadi",
    "Tidak ada akun dan tidak ada pengiriman data ke server kami",
    "Izin kamera, mikrofon, dan lokasi hanya diminta saat sebuah situs membutuhkannya"
  ],
  screenshots: [
    {
      id: "screen-1",
      title: "Browser",
      caption: "Tampilan desktop penuh dengan kursor virtual dan tombol aksi",
      src: "screens/layar-1.png",
      tag: "Layar Utama"
    },
    {
      id: "screen-2",
      title: "Tab",
      caption: "Manajemen multi-tab modern dengan pratinjau halaman",
      src: "screens/layar-2.png",
      tag: "Multi Tab"
    },
    {
      id: "screen-3",
      title: "Bookmark",
      caption: "Akses kilat ke situs kerja dan favorit Anda",
      src: "screens/layar-3.png",
      tag: "Penanda"
    },
    {
      id: "screen-4",
      title: "Riwayat",
      caption: "Daftar jejak jelajah tersusun rapi per waktu kunjungan",
      src: "screens/layar-4.png",
      tag: "Riwayat"
    },
    {
      id: "screen-5",
      title: "Pengaturan",
      caption: "Kustomisasi kursor, mesin pencari, dan kenyamanan layar",
      src: "screens/layar-5.png",
      tag: "Preferensi"
    }
  ],
  faqs: [
    {
      q: "Mengapa muncul peringatan saat memasang?",
      a: "Peringatan tersebut adalah mekanisme standar keamanan Android saat memasang aplikasi (.apk) langsung di luar Play Store. BroDesk 100% aman, bersih tanpa malware, dan tidak mengumpulkan data pribadi Anda. Anda cukup memilih 'Tetap pasang' atau izinkan 'Pasang dari sumber ini' pada browser Anda."
    },
    {
      q: "Apakah BroDesk gratis?",
      a: "Ya, BroDesk 100% gratis untuk diunduh dan digunakan tanpa biaya langganan tersembunyi maupun iklan yang mengganggu."
    },
    {
      q: "Bagaimana cara memperbarui aplikasi?",
      a: "Untuk memperbarui ke versi terbaru, cukup unduh berkas APK baru dari halaman ini dan pasang langsung di atas aplikasi lama. Semua bookmark, riwayat, dan tab Anda akan tetap utuh."
    },
    {
      q: "Di mana berkas hasil unduhan tersimpan?",
      a: "Berkas BroDesk.apk akan tersimpan di folder 'Unduhan' (Download) pada memori internal HP Anda dan dapat dibuka melalui notifikasi unduhan atau aplikasi Pengelola Berkas (File Manager)."
    }
  ]
};

// Export untuk lingkungan Node jika diuji
if (typeof module !== "undefined" && module.exports) {
  module.exports = BroDeskConfig;
}
