package com.example.myapp1.navigation;

import static nl.dionsegijn.konfetti.core.Position.Relative;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.example.myapp1.DBHelper;
import com.example.myapp1.PostingFragment;
import com.example.myapp1.R;
import com.example.myapp1.navigation.model.CollectionImgAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;


/**
 * Fragment for collection function
 * @author MW
 */
public class CollectionFragment extends Fragment {
    private static final String TAG = "COLLECTION_FRAGMENT";
    private static final int TOTAL_CLASS_NUMBER = 30;
    GridView gridView;
    CollectionManager manager;
    IconRoundCornerProgressBar progressBar;

    private final ArrayList<String> nameList = new ArrayList<>(); // class name
    private final ArrayList<Integer> idList = new ArrayList<>(); // class id( class code )

    private DBHelper dbHelper;

    // mw: fields for celebration effects
    private KonfettiView konfettiView = null;
    private Shape.DrawableShape drawableShape = null;

    public CollectionFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getContext(), PostingFragment.DB_NAME, null, 1);
        manager = CollectionManager.getInstance(); // singleton

        // mw: read json file,
        // mw: refer to TJ code(myapp1> DictionaryFragment)
        try{
            InputStream is = getResources().getAssets().open("dictionary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String buf = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(buf);
            JSONArray jsonArray = jsonObject.getJSONArray("plant");

            for(int i = 0; i < jsonArray.length(); i++) {
                String title = jsonArray.getJSONObject(i).getString("title");
                nameList.add(title.split("_")[0]);
                idList.add(Integer.parseInt(title.split("_")[1]));

            }
        }catch (IOException | JSONException e) {
            Log.e(TAG, "    onCreate() ");
            e.printStackTrace();
        }

        if (getArguments() != null) {

        }
    }

    // mw: get Detailed Description Dialog for selected class
    public void getCollectionDetailedOutputDialog(String className, int classId){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        CollectionDetailedDialogFragment dialogFragment
                = CollectionDetailedDialogFragment.newInstance(
                        className,
                        classId,
                        dbHelper.getFilePathByClassName(className)
                );
        dialogFragment.show(fragmentManager, CollectionDetailedDialogFragment.TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        /////////////////////
        // mw: for celebration Effect
        konfettiView = view.findViewById(R.id.konfettiView);
        final Drawable drawable = ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_heart);
        drawableShape = new Shape.DrawableShape(drawable, true);
        EmitterConfig emitterConfig = new Emitter(5L, TimeUnit.SECONDS).perSecond(50);
        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .shapes(new Shape.Rectangle(0.2f), drawableShape)
                .sizes(new Size(12, 5f, 0.2f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();
        konfettiView.setOnClickListener(k_view ->
                konfettiView.start(party)
        );
        //////////////////////

        gridView = view.findViewById(R.id.gridview);

        ////////////////////////////////////////////////////////
        // mw: progressBar settings
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.enableAnimation();

        progressBar.setOnIconClickListener(()->{
            int progressCount = (int)progressBar.getProgress();
            if(progressCount == TOTAL_CLASS_NUMBER){ // if user collected every collections
                explode();
                rain();
                parade();
                Toast.makeText(getContext(), progressCount + "개의 모든 컬렉션을 모으셨습니다! 축하합니다!!! ", Toast.LENGTH_SHORT).show();
                return;
            }
            String progressRate = String.format("%.2f%%", progressBar.getProgress() / TOTAL_CLASS_NUMBER*100);
            Toast.makeText(getContext(), progressCount + "개의 컬렉션을 모았어요! 진행률 : " + progressRate, Toast.LENGTH_SHORT).show();
        });
        progressBar.setAnimationSpeedScale((float)0.3);
        progressBar.setProgress(manager.getProgressCount());
        ////////////////////////////////////////////////////////

        // mw: gridView & CollectionAdapter settings
        CollectionImgAdapter adapter = new CollectionImgAdapter(gridView, getContext(),idList,nameList);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener((a_parent, a_view, a_position, a_id) -> {
            if(manager.getState(nameList.get(a_position)) == CollectionManager.ALREADY_EXIST){
                Log.e(TAG, "ALREADY EXIT 클릭됨");
                getCollectionDetailedOutputDialog(nameList.get(a_position), idList.get(a_position) ); // get Dialog

            }else {
                adapter.getView(a_position, a_view, a_parent);
                adapter.refreshImg(a_position);
            }
            progressBar.setProgress(manager.getProgressCount());
        });

        // mw: refresh collectionManager
        manager.refresh(getContext());

        return view;
    }

    // mw: methods for Celebration Effect
    // mw: Called When the user clicks the star after collecting all the collections
    public void parade() {
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(30);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.RIGHT - 45)
                        .spread(Spread.SMALL)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Relative(0.0, 0.5))
                        .build(),
                new PartyFactory(emitterConfig)
                        .angle(Angle.LEFT + 45)
                        .spread(Spread.SMALL)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(10f, 30f)
                        .position(new Relative(1.0, 0.5))
                        .build()
        );
    }
    public void explode() {
        EmitterConfig emitterConfig = new Emitter(100L, TimeUnit.MILLISECONDS).max(100);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .spread(360)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 30f)
                        .position(new Relative(0.5, 0.3))
                        .build()
        );
    }
    public void rain() {
        EmitterConfig emitterConfig = new Emitter(5, TimeUnit.SECONDS).perSecond(100);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.BOTTOM)
                        .spread(Spread.ROUND)
                        .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, drawableShape))
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .setSpeedBetween(0f, 15f)
                        .position(new Relative(0.0, 0.0).between(new Relative(1.0, 0.0)))
                        .build()
        );
    }
}