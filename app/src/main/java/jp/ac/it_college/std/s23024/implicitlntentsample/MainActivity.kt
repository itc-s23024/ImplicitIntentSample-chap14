package jp.ac.it_college.std.s23024.implicitlntentsample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import jp.ac.it_college.std.s23024.implicitlntentsample.databinding.ActivityMainBinding
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // 緯度
    private var latitude = 0.0

    // 経度
    private var longitude = 0.0

    // FusedLocation で使う３つ４つのオブジェクトを入れておくプロパティ
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // 位置情報取得の確認をするランチャーを生成する
    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // ユーザー同意ダイアログから戻ってきたらどうするかの処理をここで実装
        when {
            permissions.getOrDefault(
                android.Manifest.permission.ACCESS_FINE_LOCATION, false
            ) -> {
                startLocationUpdate()
            }
            permissions.getOrDefault(
                Manifest.permission.ACCESS_COARSE_LOCATION, false
            ) -> {
                startLocationUpdate()
            }
            else -> {
                // もし、権限がもらえなかったときにやりたいことがあれば実装する。
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ビューの設定
        binding.apply {
            // 「地図検索」ボタンのイベントを設定を
            binding.btMapSearch.setOnClickListener(::onMapSearchButtonClick)
            //  「地図表示」ボタンのイベントを設定
            binding.btMapShowCurrent.setOnClickListener(::onMapShowCurrentButtonClick)
        }
        // FusedLocationClient の取得
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // LocationRequest の生成
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()
        // LocationCallback の実装
        locationCallback = object  : LocationCallback() {
            // 教科書 リスト14.6　を変形したパターン
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // 緯度経度の取得
                    latitude = location.latitude
                    longitude = location.longitude
                    // 取得した緯度経度を画面上に表示
                    binding.tvLatitude.text = "$latitude"
                    binding.tvLongitude.text = "$longitude"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionRequestLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun onMapShowCurrentButtonClick(view: View) {
        // URI を生成する
        val uri = Uri.parse("geo:$latitude,$longitude")
        // Intent を生成して発動
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun onMapSearchButtonClick(view: View) {
        // 入力されたキーワードを取り出してURLエンコードする。
        val seachWord = binding.etSearchWord.text.toString().run {
            URLEncoder.encode(this, Charsets.UTF_8.name())
        }
        // マップアプリと連携するためのURLを生成する。
        val uri = Uri.parse("geo:0,0?q=$seachWord")
        // マップアプリ(外部アプリ)と連携する用のIntent を作って起動
        Intent(Intent.ACTION_VIEW, uri).let { intent ->
            startActivity(intent)
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, mainLooper
        )
    }
}
