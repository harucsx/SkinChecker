package me.devhi.skinchecker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dzaitsev.android.widget.RadarChartView;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;

import static android.graphics.Paint.Style.FILL_AND_STROKE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private RadarChartView radarChartView;
    private TextView txtSkinAge, txtFaceAge, txtStatusWater, txtStatusOil, txtStatusSense, txtStatusWrinkle, txtStatusPole;
    private TextView txtGender, txtGlasses, txtMakeup;
    private ImageView imgLogo;

    private JsonObject faceData;
    private JsonObject faceAttr;
    private float skinRate = 6.03F;

    private final static int REQUEST_RUN_CAMERA = 100;
    private final static int REQUEST_RUN_QNA = 101;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
//                case R.id.navigation_dashboard:
//                    changeRaderChart(1, 2, 3, 1, 0);
//                    return true;
                case R.id.navigation_notifications:
                    Intent runQna = new Intent(MainActivity.this, QnaActivity.class);
                    startActivityForResult(runQna, REQUEST_RUN_QNA);
                    return true;
                case R.id.navigation_cosmetic:
                    Toast.makeText(getApplicationContext(), "준비중 입니다.", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.navigation_product:
                    Toast.makeText(getApplicationContext(), "준비중 입니다.", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        radarChartView = (RadarChartView) findViewById(R.id.radar_chart);
        txtSkinAge = (TextView) findViewById(R.id.txtSkinAge);
        txtFaceAge = (TextView) findViewById(R.id.txtFaceAge);
        txtStatusWater = (TextView) findViewById(R.id.txtStatusWater);
        txtStatusOil = (TextView) findViewById(R.id.txtStatusOil);
        txtStatusSense = (TextView) findViewById(R.id.txtStatusSense);
        txtStatusWrinkle = (TextView) findViewById(R.id.txtStatusWrinkle);
        txtStatusPole = (TextView) findViewById(R.id.txtStatusPole);

        txtGender = (TextView) findViewById(R.id.txtGender);
        txtGlasses = (TextView) findViewById(R.id.txtGlasses);
        txtMakeup = (TextView) findViewById(R.id.txtMakeup);

        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent runCamera = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(runCamera, REQUEST_RUN_CAMERA);
            }
        });

        faceData = null;
        faceAttr = null;
        initRaderChart();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RUN_CAMERA) {
            if (resultCode == CameraActivity.SUCCESS_CALL_FACE) {
                if (data == null) return;

                String result = data.getStringExtra("response");
                try {
                    setSkinData(result);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "얼굴 인식에 실패하였습니다.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_RUN_QNA) {
            if (resultCode == QnaActivity.SUCCESS_CALL_QNA) {
                if (data == null) return;

                int water = data.getIntExtra("water", 0);
                int oil = data.getIntExtra("oil", 0);
                int sense = data.getIntExtra("sense", 0);
                int wrinkle = data.getIntExtra("wrinkle", 0);
                int pole = data.getIntExtra("pole", 0);

                skinRate = (float) (water + oil + sense + wrinkle + pole);

                Intent runCamera = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(runCamera, REQUEST_RUN_CAMERA);

                changeRaderChart(water, oil, sense, wrinkle, pole);
            }
        }
    }

    protected String indexToStatus(int index) {
        switch (index) {
            case 0:
                return "좋음";
            case 1:
                return "양호";
            case 2:
                return "주의";
            case 3:
                return "나쁨";
        }
        return "";
    }

    protected void setSkinData(String data) {
        JsonParser parser = new JsonParser();
        // Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
        JsonElement element = parser.parse(data);
        element = element.getAsJsonArray().get(0);

        faceData = element.getAsJsonObject();
        faceAttr = faceData.getAsJsonObject("faceAttributes");


        float bais = (skinRate * 2.9F / 3.1F) - 7F;

        String faceAge = faceAttr.get("age").getAsString();
        String skinAge = String.format("%.1f", Float.parseFloat(faceAge) + bais);
        String gender = faceAttr.get("gender").getAsString();
        String glasses = faceAttr.get("glasses").getAsString();

        JsonObject makeup = faceAttr.getAsJsonObject("makeup");
        boolean eyeMakeup = makeup.get("eyeMakeup").getAsBoolean();
        boolean lipMakeup = makeup.get("eyeMakeup").getAsBoolean();

        if (gender.equals("male")) {
            txtGender.setText("남성");
        } else if (gender.equals("female")) {
            txtGender.setText("여성");
        } else {
            txtGender.setText("");
        }

        if (glasses.equals("NoGlasses")) {
            txtGlasses.setText("미착용");
        } else {
            txtGlasses.setText("착용");
        }

        txtMakeup.setVisibility(View.VISIBLE);

        if (eyeMakeup && lipMakeup) {
            txtMakeup.setText("눈, 입술 화장 감지");
        } else if (eyeMakeup) {
            txtMakeup.setText("눈 화장 감지");
        } else if (lipMakeup) {
            txtMakeup.setText("입술 화장 감지");
        } else {
            txtMakeup.setVisibility(View.INVISIBLE);
            txtMakeup.setText("");
        }

        txtFaceAge.setText(faceAge + "살");
        txtSkinAge.setText(skinAge + "살");
    }

    protected void initRaderChart() {
        final Map<String, Float> axis = new LinkedHashMap<>(6);
        axis.put("유분", 0F);
        axis.put("수분", 0F);
        axis.put("민감도", 0F);
        axis.put("주름", 0F);
        axis.put("모공", 0F);

        radarChartView.setAxis(axis);
        radarChartView.setAxisMax(5F);
        radarChartView.setAutoSize(false);
        radarChartView.setChartStyle(FILL_AND_STROKE);

        // TODO : DO NOT USE THIS PARAMETER
        // radarChartView.setCirclesOnly(true);
        // radarChartView.addOrReplace("WI", 28.681F);
    }

    protected void changeRaderChart(int water, int oil, int sense, int wrinkle, int pole) {
        final Map<String, Float> axis = new LinkedHashMap<>(6);
        axis.put("유분", (float) oil);
        axis.put("수분", (float) water);
        axis.put("민감도", (float) sense);
        axis.put("주름", (float) wrinkle);
        axis.put("모공", (float) pole);

        txtStatusWater.setText(indexToStatus(water));
        txtStatusOil.setText(indexToStatus(oil));
        txtStatusSense.setText(indexToStatus(sense));
        txtStatusWrinkle.setText(indexToStatus(wrinkle));
        txtStatusPole.setText(indexToStatus(pole));

        radarChartView.setAxis(axis);
        radarChartView.setAxisMax(5F);
        radarChartView.setAutoSize(false);
        radarChartView.setChartStyle(FILL_AND_STROKE);
    }
}
