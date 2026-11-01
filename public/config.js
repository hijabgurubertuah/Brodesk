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
  aboutShort: "BroDesk adalah browser Android berantarmuka desktop nyata dengan touchpad kursor presisi, klik kanan lewat tekan lama, multi-tab, dan pintasan cepat.",
  aboutFull: "BroDesk dirancang khusus untuk kenyamanan mengakses situs desktop di ponsel Android. Dilengkapi touchpad virtual presisi, klik kanan instan, serta pintasan tombol Pilih Semua (Ctrl+A), Salin (Ctrl+C), Tempel (Ctrl+V), Hapus, dan Zoom In/Out. Seluruh data tersimpan aman secara lokal di perangkat Anda.",
  categoryChips: [
    { label: "Browser Desktop", color: "blue" },
    { label: "Kursor Touchpad", color: "purple" },
    { label: "Klik Kanan Instan", color: "emerald" },
    { label: "Tanpa Iklan", color: "amber" }
  ],
  features: [
    {
      title: "Mode Desktop",
      desc: "Tampilan PC penuh di HP",
      icon: "monitor",
      color: "#3B82F6"
    },
    {
      title: "Kursor Touchpad",
      desc: "Pointer presisi & klik responsif",
      icon: "mouse-pointer",
      color: "#8B5CF6"
    },
    {
      title: "Klik Kanan Instan",
      desc: "Tekan lama untuk menu konteks",
      icon: "hand",
      color: "#EC4899"
    },
    {
      title: "Multi Tab & Bookmark",
      desc: "Beralih situs dengan cepat",
      icon: "layers",
      color: "#10B981"
    },
    {
      title: "Pintasan Keyboard",
      desc: "Ctrl+A, C, V, Del, Zoom",
      icon: "sliders",
      color: "#F59E0B"
    }
  ],
  whatsNew: [
    "Dukungan klik responsif pada menu klik kanan (context menu) dan tutup instan saat tap di luar menu",
    "Navigasi kursor virtual presisi dengan touchpad sentuh interaktif",
    "Tombol pintasan keyboard cepat: Ctrl+A, Ctrl+C, Ctrl+V, Delete, dan Zoom In/Out",
    "Kunci layar (Screen Lock) agar kursor bebas digerakkan tanpa menggeser halaman",
    "Klik kanan instan lewat tahan lama (long-press)"
  ],
  dataSafety: [
    "Data disimpan di HP Anda tanpa pengumpulan informasi pribadi",
    "Tidak ada akun dan tidak ada pengiriman data ke server kami",
    "Izin hanya diminta saat situs tertentu membutuhkannya"
  ],
  screenshots: [
    {
      id: "screen-1",
      title: "Browser",
      caption: "Mode desktop & kursor sentuh",
      src: "screens/layar-1.png",
      tag: "Layar Utama"
    },
    {
      id: "screen-2",
      title: "Tab",
      caption: "Kelola banyak tab cepat",
      src: "screens/layar-2.png",
      tag: "Multi Tab"
    },
    {
      id: "screen-3",
      title: "Bookmark",
      caption: "Situs favorit satu sentuhan",
      src: "screens/layar-3.png",
      tag: "Penanda"
    }
  ],
  faqs: [
    {
      q: "Mengapa muncul peringatan saat memasang?",
      a: "Peringatan tersebut adalah standar keamanan Android untuk pemasangan berkas APK di luar Play Store. BroDesk 100% aman, bersih tanpa malware. Cukup pilih 'Tetap pasang' atau izinkan 'Pasang dari sumber ini'."
    },
    {
      q: "Apakah BroDesk gratis?",
      a: "Ya, BroDesk 100% gratis digunakan tanpa langganan tersembunyi maupun iklan mengganggu."
    },
    {
      q: "Bagaimana cara memperbarui aplikasi?",
      a: "Cukup unduh berkas APK terbaru dari halaman ini dan pasang di atas versi lama. Seluruh bookmark, tab, dan riwayat Anda tetap tersimpan utuh."
    },
    {
      q: "Di mana berkas hasil unduhan tersimpan?",
      a: "Berkas BroDesk.apk tersimpan di folder 'Unduhan' (Download) pada memori internal HP Anda."
    }
  ]
};

// Export untuk pengujian Node jika diperlukan
if (typeof module !== "undefined" && module.exports) {
  module.exports = BroDeskConfig;
}
