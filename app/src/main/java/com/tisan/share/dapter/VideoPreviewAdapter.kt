package com.tisan.share.dapter

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.tisan.share.datdabean.EncryptedFileItem
import com.tisan.share.utils.CryptoUtil
import java.io.File

class VideoPreviewAdapter(
    private val context: Context,
    private val videoItems: List<EncryptedFileItem>
) : RecyclerView.Adapter<VideoPreviewAdapter.VideoViewHolder>() {

    class VideoViewHolder(val playerView: PlayerView) : RecyclerView.ViewHolder(playerView) {
        var player: ExoPlayer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val playerView = PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            useController = true
        }
        return VideoViewHolder(playerView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val item = videoItems[position]

        try {
            val encBytes = File(item.filePath).readBytes()
            val decrypted = CryptoUtil.decrypt(encBytes)

            // 写入临时文件
            val tempVideoFile = File.createTempFile("video_preview_", ".mp4", context.cacheDir)
            tempVideoFile.writeBytes(decrypted)

            // 初始化播放器
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = MediaItem.fromUri(Uri.fromFile(tempVideoFile))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            holder.playerView.player = player
            holder.player = player

        } catch (e: Exception) {
            Toast.makeText(context, "解密失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        holder.player?.release()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = videoItems.size
}
