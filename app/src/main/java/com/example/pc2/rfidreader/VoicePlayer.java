package com.example.pc2.rfidreader;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class VoicePlayer
{
	private SoundPool soundPool = null;
	private int soundID = 0;
	private AudioManager am = null;
	private static VoicePlayer mInst = null;
	private float audioCurrentVolume = 0;

	static public VoicePlayer GetInst(Context ctx)
	{
		if (mInst == null)
		{
			mInst = new VoicePlayer(ctx);
		}
		return mInst;
	}

	private VoicePlayer(Context ctx)
	{
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
		soundID = soundPool.load(ctx, R.raw.msg, 1);

		am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

		audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	protected void finalize() throws Throwable
	{
		// TODO Auto-generated method stub
		if (soundPool != null)
		{
			soundPool.release();
		}
		super.finalize();
	}

	public void Play()
	{
		soundPool
				.play(soundID, audioCurrentVolume, audioCurrentVolume, 1, 0, 1);
	}
}
