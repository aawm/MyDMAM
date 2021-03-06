package hd3gtv.mydmam.transcode.mtdgenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hd3gtv.configuration.Configuration;
import hd3gtv.mydmam.Loggers;
import hd3gtv.mydmam.metadata.ContainerEntryResult;
import hd3gtv.mydmam.metadata.MetadataExtractor;
import hd3gtv.mydmam.metadata.PreviewType;
import hd3gtv.mydmam.metadata.container.Container;
import hd3gtv.mydmam.metadata.container.EntryRenderer;
import hd3gtv.mydmam.transcode.mtdcontainer.FFmpegInterlacingStats;
import hd3gtv.mydmam.transcode.mtdcontainer.FFprobe;
import hd3gtv.tools.ExecBinaryPath;
import hd3gtv.tools.ExecprocessBadExecutionException;
import hd3gtv.tools.ExecprocessGettext;
import hd3gtv.tools.StoppableProcessing;
import hd3gtv.tools.VideoConst.Interlacing;
import hd3gtv.tools.VideoConst.Resolution;

public class FFmpegInterlacingDetection implements MetadataExtractor {
	
	private int framecount;
	
	public FFmpegInterlacingDetection() {
		framecount = Configuration.global.getValue("metadata_analysing", "ffmpeg_interlacing_detection_framecount", 1000);
	}
	
	public boolean isEnabled() {
		try {
			ExecBinaryPath.get("ffmpeg");
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	public boolean canProcessThisMimeType(String mimetype) {
		if (mimetype.equalsIgnoreCase("application/gxf")) return true;
		if (mimetype.equalsIgnoreCase("application/lxf")) return true;
		if (mimetype.equalsIgnoreCase("application/mxf")) return true;
		
		if (mimetype.equalsIgnoreCase("video/mp2t")) return true;
		if (mimetype.equalsIgnoreCase("video/mp4")) return true;
		if (mimetype.equalsIgnoreCase("video/mpeg")) return true;
		if (mimetype.equalsIgnoreCase("video/quicktime")) return true;
		if (mimetype.equalsIgnoreCase("video/x-dv")) return true;
		if (mimetype.equalsIgnoreCase("video/vc1")) return true;
		if (mimetype.equalsIgnoreCase("video/mp2p")) return true;
		if (mimetype.equalsIgnoreCase("video/h264")) return true;
		if (mimetype.equalsIgnoreCase("video/x-ms-wmv")) return true;
		if (mimetype.equalsIgnoreCase("video/msvideo")) return true;
		return false;
	}
	
	public String getLongName() {
		return "Video interlacing detection type";
	}
	
	public ContainerEntryResult processFast(Container container) throws Exception {
		return null;
	}
	
	public ContainerEntryResult processFull(Container container, StoppableProcessing stoppable) throws Exception {
		
		FFprobe ffprobe = container.getByType(FFprobe.ES_TYPE, FFprobe.class);
		
		if (ffprobe == null) {
			return null;
		}
		if (ffprobe.hasVideo() == false) {
			return null;
		}
		Resolution v_res = ffprobe.getStandardizedVideoResolution();
		boolean is_valid_broadcast = false;
		if (v_res == Resolution.CIF4) is_valid_broadcast = true;
		if (v_res == Resolution.CIF9) is_valid_broadcast = true;
		if (v_res == Resolution.CIF16) is_valid_broadcast = true;
		if (v_res == Resolution.VGA) is_valid_broadcast = true;
		if (v_res == Resolution.SD_480) is_valid_broadcast = true;
		if (v_res == Resolution.SD_480_VBI) is_valid_broadcast = true;
		if (v_res == Resolution.SD_576) is_valid_broadcast = true;
		if (v_res == Resolution.SD_576_VBI) is_valid_broadcast = true;
		if (v_res == Resolution.HD_720) is_valid_broadcast = true;
		if (v_res == Resolution.HD_HALF_1080) is_valid_broadcast = true;
		if (v_res == Resolution.HD_1080) is_valid_broadcast = true;
		if (v_res == Resolution.UHD_4K) is_valid_broadcast = true;
		if (v_res == Resolution.UHD_8K) is_valid_broadcast = true;
		
		if (is_valid_broadcast == false) {
			/**
			 * Exclude all non-broadcast resolutions, to keep only potentially interlaced formats.
			 */
			return null;
		}
		
		ArrayList<String> param = new ArrayList<String>();
		param.add("-threads");
		param.add("4");
		param.add("-i");
		param.add(container.getPhysicalSource().getPath());
		param.add("-filter:v");
		param.add("idet");
		param.add("-frames:v");
		param.add(String.valueOf(framecount));
		param.add("-an");
		param.add("-f");
		param.add("md5");
		param.add("-y");
		param.add("-");
		
		ExecprocessGettext process = new ExecprocessGettext(ExecBinaryPath.get("ffmpeg"), param);
		process.setEndlinewidthnewline(true);
		Loggers.Transcode.debug("Start FFmpeg Interlacing detection\t" + process.getRunprocess().getCommandline());
		
		try {
			process.start();
		} catch (IOException e) {
			if (e instanceof ExecprocessBadExecutionException) {
				if (process.getRunprocess().getExitvalue() == 1) {
					Loggers.Transcode_Metadata.error("Invalid data found when processing input, " + process + ", " + container);
				} else {
					Loggers.Transcode_Metadata.error("Problem with ffmpeg, " + process + ", " + container);
				}
			}
			throw e;
		}
		
		String[] lines = process.getResultstderr().toString().split(ExecprocessGettext.LINESEPARATOR);
		String current_line;
		String item;
		String item_single = null;
		String item_multi = null;
		for (int pos_ln = 0; pos_ln < lines.length; pos_ln++) {
			current_line = lines[pos_ln];
			if (current_line.startsWith("[Parsed_idet_0 @") == false) {
				continue;
			}
			// [Parsed_idet_0 @ 0x7f8818f01e20] Repeated Fields: Neither: 1129 Top: 0 Bottom: 0
			// [Parsed_idet_0 @ 0x7f8818f01e20] Single frame detection: TFF: 227 BFF: 214 Progressive: 578 Undetermined: 110
			// [Parsed_idet_0 @ 0x7f8818f01e20] Multi frame detection: TFF: 249 BFF: 256 Progressive: 589 Undetermined: 35
			
			item = current_line.substring(current_line.indexOf("]") + 1).trim();
			if (item.startsWith("Single frame detection:")) {
				item_single = item.substring("Single frame detection:".length()).trim();
				continue;
			}
			if (item.startsWith("Multi frame detection:")) {
				item_multi = item.substring("Multi frame detection:".length()).trim();
				continue;
			}
		}
		
		FFmpegInterlacingStats stats = FFmpegInterlacingStats.parseFFmpegResult(item_single, item_multi);
		
		if (stats == null) {
			return null;
		}
		Interlacing stats_interlace = stats.getInterlacing();
		switch (stats_interlace) {
		case Progressive:
			container.getSummary().putSummaryContent(stats, "Progressive frames");
			break;
		case TopFieldFirst:
			container.getSummary().putSummaryContent(stats, "Interlaced, top field first (odd)");
			break;
		case BottomFieldFirst:
			container.getSummary().putSummaryContent(stats, "Interlaced, bottom field first (even)");
			break;
		case Unknow:
			container.getSummary().putSummaryContent(stats, "Some interlaced and progressive frames");
			break;
		}
		return new ContainerEntryResult(stats);
	}
	
	public boolean isTheExtractionWasActuallyDoes(Container container) {
		return container.containAnyMatchContainerEntryType(FFmpegInterlacingStats.ES_TYPE);
	}
	
	public List<String> getMimeFileListCanUsedInMasterAsPreview() {
		return null;
	}
	
	public boolean isCanUsedInMasterAsPreview(Container container) {
		return false;
	}
	
	public PreviewType getPreviewTypeForRenderer(Container container, EntryRenderer entry) {
		return null;
	}
	
}
