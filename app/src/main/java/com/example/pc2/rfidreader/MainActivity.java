package com.example.pc2.rfidreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pc2.rfidreader.Model.TagInfo;
import com.rfid.api.ADReaderInterface;
import com.rfid.def.ApiErrDefinition;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TagListener {

    private TagReader tagReader;

    DataAdapter adapter;

    Button scanButton;

    ListView listView;

    int initialTagCount;

    private List<TagInfo> list = new ArrayList<TagInfo>();

    private List<String> statusList = new ArrayList<String>();

    private ADReaderInterface m_reader = new ADReaderInterface();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scan);

        statusList.add("Beklenen");

        statusList.add("Tanımsız");

        statusList.add("Onaylanan");

        adapter = new DataAdapter(list,this,statusList);

        listView = findViewById(R.id.liste);

        listView.setAdapter(adapter);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


stopTagScanning();
            }
        });
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {  //Tarama başlatılması için * tuşuna basılır
        //Toast.makeText(this, KeyEvent.keyCodeToString(keyCode), Toast.LENGTH_SHORT).show();
        switch (keyCode) {
            case   KeyEvent.KEYCODE_STAR:
            toggleTagScanner();


                break;
        }
        return true;
    }

    private void toggleTagScanner() {



        if (m_reader.isReaderOpen()) {
            stopTagScanning();
        } else {
            startTagScanning();
        }

    }


    private void stopTagScanning() {

        if (m_reader != null) {
            m_reader.RDR_Close();
        }
        if (tagReader != null) {
            tagReader.stop();
            Toast.makeText(this, getString(R.string.stop_identity), Toast.LENGTH_SHORT).show();
        }
    }

    private void startTagScanning() {

        String conStr = String
                .format("RDType=%s;CommType=COM;ComPath=%s;Baund=%s;Frame=%s;Addr=255",
                        "AH2201", "/dev/ttyMT1",
                        "38400",
                        "8E1");

        Log.d("stringDevice",conStr);
        Log.d("readControl", String.valueOf(m_reader.RDR_Open(conStr)));
        Log.d("api error", String.valueOf(ApiErrDefinition.NO_ERROR));

        if (m_reader.RDR_Open(conStr) == ApiErrDefinition.NO_ERROR) {


            tagReader = new TagReader(m_reader,  this);


            new Thread(tagReader).start();

            Toast.makeText(MainActivity.this, getString(R.string.start_identity), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Cihaz bağlantısı kurulamadı.", Toast.LENGTH_SHORT).show();
        }


    }

    private int findTag(String uid) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTagUid().equals(uid)) {
                return i;
            }
        }
        return -1;
        /*
        for (TagInfo tagInfo : list) {
            if (tagInfo.getTagUid().equals(uid)) {
                return tagInfo;
            }
        }
        return null;
        */
    }

    //Okunan tag bilgisi alınır ve türü belirlenir
    public void onTagRead(String uid) {

        TagInfo tag = null;
        int tagIndex = findTag(uid);
        if (tagIndex >= 0) {
            tag = list.get(tagIndex);
        }

        if (tag == null) { // New (unexpected) tag found // barkod ilk okutulduğuna
            TagInfo newTag = new TagInfo();
            newTag.setTagUid(uid);
            newTag.setStatus("İlk");
            list.add(newTag);
            adapter.update(list);
            VoicePlayer.GetInst(this).Play();
        }
        else if (tag.getStatus().equals("")) { // Tag marked as scanned
            tag.setStatus("tekrar");
            list.add(initialTagCount, tag);
            list.remove(tagIndex);
            adapter.update(list);
           // updateViewCount();
            VoicePlayer.GetInst(this).Play();
        }

    }


}