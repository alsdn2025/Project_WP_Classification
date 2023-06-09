package com.example.myapp1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapp1.Cam.CameraActivity
import com.example.myapp1.databinding.ActivityMainBinding
import com.example.myapp1.navigation.CollectionFragment
import com.example.myapp1.navigation.CollectionManager
import com.example.myapp1.navigation.DetailViewFragment
import com.example.myapp1.navigation.SearchFragment
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    //현재 위치 관련
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    var templat: Double? = 0.0 //현재 위치 위도를 저장할 변수
    var templong: Double? = 0.0 //현재 위치 경도를 저장할 변수
    var updatecheck: Int = 0 //최신 위차가 갱신됐는지 체크하기 위해!

    //현재 위치 관련
    private var mFusedLocationProviderClient: FusedLocationProviderClient? =
        null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10
    //요기까지 Gps 관련 부분

    // Camera Permission
    private val REQUEST_PERMISSION_CAMERA = 1000


    private lateinit var binding2: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener(this)

        // set default screen
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.action_home


        //위치 저장 관련
        //DB 관련 부분
        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        //Gps기능 사용하기 위해
        mLocationRequest = LocationRequest.create().apply {
            interval = 1000//주기 갱신을 1000ms, 1초마다 해달라고 요청, 11/27 추가
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        }
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }//요기까지 잘 실행되면, 주기적으로 위치 갱신을 요청합니다~라고 구글 서비스에 알림->정상 작동하면
        // 앱 위에 gps마커 나타남


        // CollectionManager (singleton class)
        val manager: CollectionManager = CollectionManager.getInstance()
        manager.init(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }
            R.id.action_search ->{
                var gridFragment = SearchFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }

            // mw : 카메라 권한 요청
            // onCreate 에서 권한 요청시 Google Map 관련 라이브러리와 충돌이 일어남, 따로 뺌
            R.id.action_add_photo ->{
                if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
                    Toast.makeText(this, "권한 승인 후 다시 한 번 카메라 버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    val cameraIntent = Intent(this, CameraActivity::class.java);
                    startActivity(cameraIntent)
                }
                return true
            }
            R.id.action_favorite_alarm ->{
                // var alarmFragment = AlarmFragment()
                // supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()

                val mapintent = Intent(this, MapActivity::class.java)
                startActivity(mapintent)

                return true
            }
            R.id.action_account ->{
                var collectionFragment = CollectionFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,collectionFragment).commit()

                return true
            }
        }
        return false
    }

    //여기부터 코드 끝까지는 GPS기능을 사용하기 위해 써줘야 하는 함수(서로 연결되어 있으므로, 한 덩어리라고 생각해도 좋음)
    private fun startLocationUpdates() {
        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location
        templat = mLastLocation.latitude + 0.0001 // 갱신된 위도
        templong = mLastLocation.longitude + 0.0001 // 갱신된 경도
        updatecheck = 1 //templat,templong에 최신 위치가 갱신 완료되었음을 체크하기 위해
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode == REQUEST_PERMISSION_CAMERA){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { //거부
                Toast.makeText(this@MainActivity, "카메라 권한이 거부됨", Toast.LENGTH_SHORT).show()
            }
        }
    }
}