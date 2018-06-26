package com.example.pc2.rfidreader;



import android.os.Handler;
import android.os.Message;

import com.rfid.api.ADReaderInterface;
import com.rfid.api.GFunction;
import com.rfid.api.ISO14443AInterface;
import com.rfid.api.ISO14443ATag;
import com.rfid.api.ISO15693Interface;
import com.rfid.api.ISO15693Tag;
import com.rfid.def.ApiErrDefinition;
import com.rfid.def.RfidDef;

import java.util.Vector;

/**
 * Created by onurays on 14/10/2017.
 */

public class TagReader implements Runnable {

    private static final int INVENTORY_MSG = 1;
    private static final int GETSCANRECORD = 2;
    private static final int INVENTORY_FAIL_MSG = 4;
    private static final int THREAD_END = 3;

    private final ADReaderInterface m_reader;
    private TagListener tagListener;
    private Handler mHandler;

    private boolean bOnlyReadNew = false;
    private long mAntCfg = 0x000000;
    private boolean bUseISO15693 = false;
    private boolean bUseISO14443A = false;
    private boolean bMathAFI = false;
    private byte mAFIVal = 0x00;
    private long mLoopCnt = 0;
    private boolean b_inventoryThreadRun;

    public TagReader(ADReaderInterface m_reader, MainActivity tagListener) {
        this.m_reader = m_reader;
        this.tagListener = (TagListener) tagListener;
        mHandler = new TagHandler( tagListener);
    }

    public void stop() {
        b_inventoryThreadRun = false;
    }

    @Override
    public void run() {
        {
            int failedCnt = 0;// ����ʧ�ܴ���
            Object hInvenParamSpecList = null;
            byte newAI = RfidDef.AI_TYPE_NEW;
            byte useAnt[] = null;
            if (bOnlyReadNew) {
                newAI = RfidDef.AI_TYPE_CONTINUE;
            }

            if (mAntCfg != 0) {
                Vector<Byte> vAntList = new Vector<Byte>();
                for (int i = 0; i < 32; i++) {
                    if ((mAntCfg & (1 << i)) != 0) {
                        vAntList.add((byte) (i + 1));
                    }
                }

                useAnt = new byte[vAntList.size()];
                for (int i = 0; i < useAnt.length; i++) {
                    useAnt[i] = vAntList.get(i);
                }
            }

            if (bUseISO14443A || bUseISO15693) {
                hInvenParamSpecList = ADReaderInterface
                        .RDR_CreateInvenParamSpecList();
                if (bUseISO15693) {
                    ISO15693Interface.ISO15693_CreateInvenParam(
                            hInvenParamSpecList, (byte) 0, bMathAFI, mAFIVal,
                            (byte) 0);
                }
                if (bUseISO14443A) {
                    ISO14443AInterface.ISO14443A_CreateInvenParam(
                            hInvenParamSpecList, (byte) 0);
                }
            }

            mLoopCnt = 0;
            b_inventoryThreadRun = true;
            while (b_inventoryThreadRun) {
                try {
                    if (mHandler.hasMessages(INVENTORY_MSG)) {
                        continue;
                    }
                    int iret = m_reader.RDR_TagInventory(newAI, useAnt, 0,
                            hInvenParamSpecList);
                    if (iret == ApiErrDefinition.NO_ERROR
                            || iret == -ApiErrDefinition.ERR_STOPTRRIGOCUR) {
                        Vector<Object> tagList = new Vector<Object>();
                        newAI = RfidDef.AI_TYPE_NEW;
                        if (bOnlyReadNew
                                || iret == -ApiErrDefinition.ERR_STOPTRRIGOCUR) {
                            newAI = RfidDef.AI_TYPE_CONTINUE;
                        }

                        Object tagReport = m_reader
                                .RDR_GetTagDataReport(RfidDef.RFID_SEEK_FIRST);
                        while (tagReport != null) {
                            ISO15693Tag ISO15693TagData = new ISO15693Tag();
                            iret = ISO15693Interface.ISO15693_ParseTagDataReport(
                                    tagReport, ISO15693TagData);
                            if (iret == ApiErrDefinition.NO_ERROR) {
                                // ISO15693 TAG
                                tagList.add(ISO15693TagData);
                                tagReport = m_reader
                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                continue;
                            }

                            ISO14443ATag ISO14444ATagData = new ISO14443ATag();
                            iret = ISO14443AInterface.ISO14443A_ParseTagDataReport(
                                    tagReport, ISO14444ATagData);
                            if (iret == ApiErrDefinition.NO_ERROR) {
                                // ISO14443A TAG
                                tagList.add(ISO14444ATagData);
                                tagReport = m_reader
                                        .RDR_GetTagDataReport(RfidDef.RFID_SEEK_NEXT);
                                continue;
                            }
                        }

                        mLoopCnt++;
                        Message msg = mHandler.obtainMessage();
                        msg.what = INVENTORY_MSG;
                        msg.obj = tagList;
                        msg.arg1 = failedCnt;
                        mHandler.sendMessage(msg);
                    } else {
                        mLoopCnt++;
                        newAI = RfidDef.AI_TYPE_NEW;
                        if (b_inventoryThreadRun) {
                            failedCnt++;
                        }
                        Message msg = mHandler.obtainMessage();
                        msg.what = INVENTORY_FAIL_MSG;
                        msg.arg1 = failedCnt;
                        mHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            b_inventoryThreadRun = false;
            m_reader.RDR_ResetCommuImmeTimeout();
            mHandler.sendEmptyMessage(THREAD_END);// �̵����
        }
    }

    private static class TagHandler extends Handler {

        private TagListener tagListener;

        public TagHandler(TagListener tagListener) {

            this.tagListener = tagListener;
        }

        @Override
        public void handleMessage(Message msg) {
            boolean b_find = false;
            switch (msg.what) {
                case INVENTORY_MSG:// �̵㵽��ǩ

                    @SuppressWarnings("unchecked")
                    Vector<Object> tagList = (Vector<Object>) msg.obj;
                    /*
                    if (pt.bRealShowTag && !pt.inventoryList.isEmpty()) {
                        pt.inventoryList.clear();
                    }
                    if (!tagList.isEmpty()) {
                        VoicePlayer.GetInst(pt).Play();
                    }
                    */
                    for (int i = 0; i < tagList.size(); i++) {
                        b_find = false;

                        // ISO15693 TAG
                        if (tagList.get(i) instanceof ISO15693Tag) {
                            ISO15693Tag tagData = (ISO15693Tag) tagList.get(i);
                            String uidStr = GFunction.encodeHexStr(tagData.uid);
                            tagListener.onTagRead(uidStr);
                            /*
                            for (int j = 0; j < pt.inventoryList.size(); j++) {
                                InventoryReport mReport = pt.inventoryList.get(j);
                                if (mReport.getUidStr().equals(uidStr)) {
                                    mReport.setFindCnt(mReport.getFindCnt() + 1);
                                    b_find = true;
                                    break;
                                }
                            }
                            if (!b_find) {
                                long mCnt = pt.bRealShowTag ? 0 : 1;
                                String tagName = ISO15693Interface
                                        .GetTagNameById(tagData.tag_id);
                                pt.inventoryList.add(new InventoryReport(uidStr,
                                        tagName, mCnt));

                            }
                            */
                        } else if (tagList.get(i) instanceof ISO14443ATag) {
                            ISO14443ATag tagData = (ISO14443ATag) tagList.get(i);
                            String uidStr = GFunction.encodeHexStr(tagData.uid);
                            tagListener.onTagRead(uidStr);
                            /*
                            for (int j = 0; j < pt.inventoryList.size(); j++) {
                                InventoryReport mReport = pt.inventoryList.get(j);
                                if (mReport.getUidStr().equals(uidStr)) {
                                    mReport.setFindCnt(mReport.getFindCnt() + 1);
                                    b_find = true;
                                    break;
                                }
                            }
                            if (!b_find) {
                                long mCnt = pt.bRealShowTag ? 0 : 1;
                                String tagName = ISO14443AInterface
                                        .GetTagNameById(tagData.tag_id);
                                pt.inventoryList.add(new InventoryReport(uidStr,
                                        tagName, mCnt));

                            }
                            */
                        }

                    }
                    /*
                    pt.tv_inventoryInfo.setText(pt
                            .getString(R.string.tx_info_tagCnt)
                            + pt.inventoryList.size()
                            + pt.getString(R.string.tx_info_loopCnt) + pt.mLoopCnt
                            + pt.getString(R.string.tx_info_failCnt) + msg.arg1);
                    pt.inventoryAdapter.notifyDataSetChanged();
                    */
                    break;
                case INVENTORY_FAIL_MSG:
                    /*
                    pt.tv_inventoryInfo.setText(pt
                            .getString(R.string.tx_info_tagCnt)
                            + pt.inventoryList.size()
                            + pt.getString(R.string.tx_info_loopCnt) + pt.mLoopCnt
                            + pt.getString(R.string.tx_info_failCnt) + msg.arg1);
                    */
                    break;
                case GETSCANRECORD:// ɨ�赽��¼
                    /*
                    @SuppressWarnings("unchecked")
                    Vector<String> dataList = (Vector<String>) msg.obj;
                    for (String str : dataList) {
                        b_find = false;
                        for (int i = 0; i < pt.scanfReportList.size(); i++) {
                            ScanReport mReport = pt.scanfReportList.get(i);
                            if (str.equals(mReport.getDataStr())) {
                                mReport.setFindCnt(mReport.getFindCnt() + 1);
                                b_find = true;
                            }
                        }
                        if (!b_find) {
                            pt.scanfReportList.add(new ScanReport(str));
                        }

                    }
                    pt.tv_scanRecordInfo.setText(pt
                            .getString(R.string.tx_info_scanfCnt)
                            + pt.scanfReportList.size());
                    pt.scanfAdapter.notifyDataSetChanged();
                    */
                    break;
                case THREAD_END:// �߳̽���
                    // pt.FinishInventory();
                    break;
                default:
                    break;
            }
        }
    }

    public static class InventoryReport {
        private String uidStr;
        private String TagTypeStr;
        private long findCnt = 0;

        public InventoryReport() {
            super();
        }

        public InventoryReport(String uid, String tayType, long cnt) {
            super();
            this.setUidStr(uid);
            this.setTagTypeStr(tayType);
            this.setFindCnt(cnt);
        }

        public String getUidStr() {
            return uidStr;
        }

        public void setUidStr(String uidStr) {
            this.uidStr = uidStr;
        }

        public String getTagTypeStr() {
            return TagTypeStr;
        }

        public void setTagTypeStr(String tagTypeStr) {
            TagTypeStr = tagTypeStr;
        }

        public long getFindCnt() {
            return findCnt;
        }

        public void setFindCnt(long findCnt) {
            this.findCnt = findCnt;
        }
    }
}
