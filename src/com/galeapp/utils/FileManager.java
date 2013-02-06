package com.galeapp.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import com.galeapp.backgrounds.Constants;

public class FileManager {

	public static final String TAG = "FileManager";

	public static void makeDir(String path) {
		File dirFile = new File(getExternalStoragePath() + path);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
	}

	public static String getThumbnailPath() {
		return getExternalStoragePath() + Constants.THUMBNAIL_PATH;
	}

	public static String getThumbnailFile(int imageId) {
		return getExternalStoragePath() + Constants.THUMBNAIL_PATH + imageId;
	}

	public static String getSavePath() {
		return getExternalStoragePath() + Constants.SAVE_PATH;
	}

	public static String getSaveFile(int imageId) {
		return getExternalStoragePath() + Constants.SAVE_PATH + imageId
				+ ".jpg";
	}

	public static String getSaveFile(String fileName) {
		return getExternalStoragePath() + Constants.SAVE_PATH + fileName;
	}

	public static boolean isSaveFileExited(int imageId) {
		File file = new File(getSaveFile(imageId));
		return file.exists();
	}

	public static String getPreviewPath() {
		return getExternalStoragePath() + Constants.PRIVIEW_PATH;
	}

	public static String getResultPath() {
		return getExternalStoragePath() + Constants.RESULT_PATH;
	}

	public static void createBackgroundsFile() {
		File dirFile = new File(getExternalStoragePath()
				+ Constants.BACKGROUNDS_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
	}

	public static void saveSaveBitmap(Bitmap bitmap, int imageId)
			throws Exception {
		// 假如存储卡可用空间小于1M,不缓存图片
		if (getAvailableStore(getExternalStoragePath()) < 1024000) {
			return;
		}
		if (bitmap == null) {
			return;
		}

		File dirFile = new File(getExternalStoragePath() + Constants.SAVE_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File saveFile = new File(getSavePath() + imageId + ".jpg");

		saveFile.deleteOnExit();
		saveFile.createNewFile();
		BufferedOutputStream bos;
		bos = new BufferedOutputStream(new FileOutputStream(saveFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}

	public static void saveThumbnailBitmap(Bitmap bitmap, int imageId)
			throws Exception {
		// 假如存储卡可用空间小于20k,不缓存图片
		if (getAvailableStore(getExternalStoragePath()) < 20240) {
			Log.i(TAG, "存储卡可用空间小于20k,不缓存图片");
			return;
		}
		if (bitmap == null) {
			return;
		}
		File dirFile = new File(getExternalStoragePath()
				+ Constants.THUMBNAIL_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File thumbnailFile = new File(getThumbnailPath() + imageId);
		Log.i(TAG, "thumbnailFile:" + thumbnailFile.getAbsolutePath());

		thumbnailFile.deleteOnExit();
		thumbnailFile.createNewFile();
		BufferedOutputStream bos;
		bos = new BufferedOutputStream(new FileOutputStream(thumbnailFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
		bos.flush();
		bos.close();
	}

	public static void savePreviewBitmap(Bitmap bitmap) throws Exception {
		// 假如存储卡可用空间小于100k,不缓存图片
		if (getAvailableStore(getExternalStoragePath()) < 102400) {
			return;
		}
		if (bitmap == null) {
			return;
		}
		File dirFile = new File(getExternalStoragePath()
				+ Constants.THUMBNAIL_PATH);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File previewFile = new File(getPreviewPath());
		Log.i(TAG, "previewFile:" + previewFile.getAbsolutePath());

		previewFile.deleteOnExit();
		previewFile.createNewFile();
		BufferedOutputStream bos;
		bos = new BufferedOutputStream(new FileOutputStream(previewFile));
		bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
		bos.flush();
		bos.close();
	}

	// json数据缓存之内存
	public static void writeJsonCacheFile(String fileName, Context context,
			String json) {
		OutputStream oStream = null;
		OutputStreamWriter oStreamWriter = null;
		try {
			oStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			oStreamWriter = new OutputStreamWriter(oStream);
			oStreamWriter.write(json);
			oStreamWriter.close();
			oStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取SdCard路径
	 */
	public static String getExternalStoragePath() {
		// 获取SdCard状态
		String state = android.os.Environment.getExternalStorageState();
		// 判断SdCard是否存在并且是可用的
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
				// Log.i(TAG,
				// android.os.Environment.getExternalStorageDirectory().getPath());
				return android.os.Environment.getExternalStorageDirectory()
						.getPath();
			}
		}
		return "/sdcard";
	}

	/**
	 * 获取存储卡的剩余容量，单位为字节(byte)
	 * 
	 * @param filePath
	 * @return availableSpare
	 */
	public static long getAvailableStore(String filePath) {
		// 取得sdcard文件路径
		StatFs statFs = new StatFs(filePath);
		// 获取block的SIZE
		long blocSize = statFs.getBlockSize();
		// 获取BLOCK数量
		// long totalBlocks = statFs.getBlockCount();
		// 可使用的Block的数量
		long availaBlock = statFs.getAvailableBlocks();
		// long total = totalBlocks * blocSize;
		long availableSpare = availaBlock * blocSize;
		// Log.i(TAG, "可用空间："+availableSpare/1024+"k");
		return availableSpare;
	}

	/**
	 * 获取存储卡的剩余容量，单位为K
	 * 
	 * @param filePath
	 * @return availableSpare
	 */
	public static int getSdcardAvailableStore() {
		// 取得sdcard文件路径
		StatFs statFs = new StatFs(getExternalStoragePath());
		// 获取block的SIZE
		long blocSize = statFs.getBlockSize();
		// 获取BLOCK数量
		// long totalBlocks = statFs.getBlockCount();
		// 可使用的Block的数量
		long availaBlock = statFs.getAvailableBlocks();
		// long total = totalBlocks * blocSize;
		long availableSpare = availaBlock * blocSize;
		Log.i(TAG,
				"availableSpare:"
						+ availableSpare
						+ " size:"
						+ Integer.valueOf(String.valueOf(availableSpare / 1024)));
		return Integer.valueOf(String.valueOf(availableSpare / 1024));
	}

	// 拷贝文件
	public static void copyFile(String pathOld, String pathNew) {
		File fileOld = new File(pathOld);
		File fileNew = new File(pathNew);

		if (fileOld.exists()) {
			fileNew.deleteOnExit();
			try {
				FileInputStream fis = new FileInputStream(fileOld);
				FileOutputStream fos = new FileOutputStream(fileNew);
				int read = 0;
				while ((read = fis.read()) != -1) {
					fos.write(read);
					fos.flush();
				}
				fos.close();
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	// 获取文件大小
	public static long getFileSize(String sPath) {
		File file = new File(sPath);
		// 路径为文件且为空则返回0值
		if (file.isFile() && file.exists()) {
			return file.length();
		} else {
			return 0;
		}
	}

	// 获取文件目录大小
	public static long getDirectorySize(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return 0;
		}
		// 获取文件夹下的所有文件大小(包括子目录)
		long directorySize = 0;
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 获取子文件大小
			if (files[i].isFile()) {
				directorySize += getFileSize(files[i].getAbsolutePath());
			} // 获取子目录文件大小
			else {
				directorySize += getDirectorySize(files[i].getAbsolutePath());
			}
		}
		return directorySize;
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 清空目录（文件夹）
	 * 
	 * @param sPath
	 *            被清空目录的文件路径
	 * @return 目录清空成功返回true，否则返回false
	 */
	public static boolean clearDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 清空文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// //删除当前目录
		// if (dirFile.delete()) {
		// return true;
		// } else {
		// return false;
		// }
		if (dirFile.list().length == 0) {
			return true;
		} else {
			return false;
		}
	}
}