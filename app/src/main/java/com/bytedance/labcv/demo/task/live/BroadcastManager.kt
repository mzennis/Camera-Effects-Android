package com.bytedance.labcv.demo.task.live

import android.net.Uri
import android.util.Pair
import android.view.SurfaceHolder
import org.json.JSONObject

/**
 * Sample Implementation of our current Live Stream SDK
 */
class BroadcastManager : Streamer.Listener {

    private var mStreamer: Streamer? = null

    fun create(holder: SurfaceHolder, size: Pair<Int, Int>) {

        val builder = StreamerBuilder()
        builder.setListener(this)

        builder.setAudioConfig(audioConfig)
        builder.setVideoConfig(videoConfig)
        builder.setSurface(holder.surface) // we just have to set the surface, and the rest will be handled by the SDK
        builder.setSurfaceSize(size.first, size.second) // width, height
        builder.addCamera(cameraConfig)

        mStreamer = builder.build()
    }

    fun flip() {
        mStreamer.flip()
    }

    fun start(ingestUrl: String) {
        mStreamer.start(ingestUrl)
    }

    fun stop() {
        mStreamer.stop()
    }

    fun release() {
        mStreamer.release()
        mStreamer = null
    }

    override fun onConnectionStateChanged(
        connectionId: Int,
        state: State,
        status: Status,
        info: JSONObject?,
    ) {
        // handle streamer connection state
    }

    override fun onVideoCaptureStateChanged(state: State) {
        // handle camera & video state
    }

    override fun onAudioCaptureStateChanged(state: State) {
        // handle audio state
    }

    override fun onRecordStateChanged(
        state: State,
        uri: Uri?,
        method: Method,
    ) {
        // unused, can be ignored at least for now
    }

    override fun onSnapshotStateChanged(
        state: State,
        uri: Uri?,
        method: Method,
    ) {
    }
}