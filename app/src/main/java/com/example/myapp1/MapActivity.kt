package com.example.myapp1

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener


/**
 * @author jc
 */
class MapActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    var googleMap: GoogleMap? = null

    //db 관련 변수 설정
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    //요기까지 DB 관련 변수 설정


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.all { permission -> permission.value == true }) {
                // 위치 제공자 준비하기
                apiClient.connect()
            } else {
                Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }

        (supportFragmentManager.findFragmentById(R.id.mapViewing) as
                SupportMapFragment?)!!.getMapAsync(this)
        providerClient = LocationServices.getFusedLocationProviderClient(this)
        apiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) !==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_NETWORK_STATE
            ) !==
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
            )
        } else {
            // 위치 제공자 준비하기
            apiClient.connect()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults[0] === PackageManager.PERMISSION_GRANTED &&
            grantResults[1] === PackageManager.PERMISSION_GRANTED &&
            grantResults[2] === PackageManager.PERMISSION_GRANTED
        ) {
            apiClient.connect()
        }
    }
    private fun moveMap(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val position: CameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(16f)
            .build()
        // 지도 중심 이동하기
        googleMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(position))
        // 마커 옵션
        val markerOptions = MarkerOptions()
        markerOptions.icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
        )

        markerOptions.position(latLng)
        markerOptions.title("My Location")
        // 마커 표시하기
        googleMap?.addMarker(markerOptions)


        //db 활용한 추가 마커 표시
        dbHelper = DBHelper(this, "mw_temp.db", null, 1)

        //민우가 만든 db와 연동되는지 테스트 "mw_temp.db"
        //dbHelper = DBHelper(this, "mw_temp.db", null, 1)

        database = dbHelper.writableDatabase
        //db와 연동하고

        var datanum: Int = 0 //저장된 식물 위치 정보가 몇 개인지 세줄 용도
        var idlist = mutableListOf<Int>()
        var latlist = mutableListOf<Double>() // 위도 저장용 리스트
        var longlist = mutableListOf<Double>() //경도 저장용 리스트
        var classlist = mutableListOf<String>()
        var filenamelist = mutableListOf<String>()
        var commentlist = mutableListOf<String>()
        var query = "SELECT * FROM location;" //db의 location 테이블에서 전부 가져온다
        var cursor = database.rawQuery(query, null)
        while (cursor.moveToNext()) {//커서가 db의 한 행씩 내려가면서, 끝까지 반복
            var id=cursor.getInt(cursor.getColumnIndex("id"))
            var lat = cursor.getDouble(cursor.getColumnIndex("lat"))
            //DB location 테이블의 lat속성의 위도 값을 변수 lat에 저장
            var long = cursor.getDouble(cursor.getColumnIndex("long"))
            //DB location 테이블의 long속성의 경도 값을 변수 long에 저장
            var thisclass = cursor.getString(cursor.getColumnIndex("class"))
            var thisfilename = cursor.getString(cursor.getColumnIndex("filename"))
            var thiscommentname = cursor.getString(cursor.getColumnIndex("comment"))
            idlist.add(id)
            latlist.add(lat) //위도용 테이블에 이번 위도값 저장
            longlist.add(long) //경도용 테이블에 이번 경도값 저장
            classlist.add(thisclass)
            filenamelist.add(thisfilename)
            commentlist.add(thiscommentname)
            datanum = datanum + 1 //데이터 개수 한 개 증가시켜주기
        }

        //db에서 추가 마커 데이터 가져오기
        var markers = MarkerOptions()
        markers.icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )
        if (datanum >= 1) {//db에 저장한 식물 개수가 1개 이상일 때만 추가 마커 표시할 거니까
            for (count in 1..datanum) {//datanum번 만큼 반복
                markers.position(LatLng(latlist[count - 1], longlist[count - 1]))//인덱스니까 -1해주고
                markers.title("${classlist[count-1]}${idlist[count-1]}, ${filenamelist[count-1]}${idlist[count-1]}, ${commentlist[count-1]}${idlist[count-1]}")//마커 클릭하면 보일 문자열
                googleMap?.addMarker(markers)//마커 표시
            }
        }
    }
    // 위치 제공자를 사용할 수 있는 상황일 때
    override fun onConnected(p0: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                this@MapActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            providerClient.getLastLocation().addOnSuccessListener(
                this@MapActivity,
                object : OnSuccessListener<Location> {
                    override fun onSuccess(location: Location?) {
                        location?.let {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            // 지도 중심 이동하기
                            moveMap(latitude, longitude)
                        }
                    }
                })
            apiClient.disconnect()
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        // 위치 제공자를 사용할 수 없을 때
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // 사용할 수 있는 위치 제공자가 없을 때
    }

    // 지도 객체를 이용할 수 있는 상황이 될 때
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
    }
}