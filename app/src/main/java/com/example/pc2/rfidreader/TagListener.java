package com.example.pc2.rfidreader;

/**
 * Created by onurays on 15/10/2017.
 */

public interface TagListener {

    void onTagRead(String uid);
}
