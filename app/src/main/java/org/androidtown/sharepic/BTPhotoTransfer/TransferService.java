package org.androidtown.sharepic.BTPhotoTransfer;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.androidtown.sharepic.Album;
import org.androidtown.sharepic.AlbumDBHandler;
import org.androidtown.sharepic.MyApplication;
import org.androidtown.sharepic.Photo;
import org.androidtown.sharepic.btxfr.ClientThread;
import org.androidtown.sharepic.btxfr.MessageType;
import org.androidtown.sharepic.btxfr.ServerThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TransferService extends Service {
    private static final String TAG = "BTPHOTO/TransferService";

    ContentResolver resolver;
    ContentObserver observer;
    SharedPreferences preferences;
    ArrayList<String> filePaths;
    ArrayList<Uri> uris;
    private String fileName; //찍은 사진파일 이름
    File file;

    ///////////// 앨범 추가 기능 /////////////
    private ContentResolver contentResolver;

    public TransferService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init2();
    }

    public void init2() {
        contentResolver = this.getContentResolver();

        BTService.clientHandler = new Handler() {

            @Override
            public void handleMessage(Message message) { //보내는 쪽
                switch (message.what) {
                    case MessageType.READY_FOR_DATA: {
                        try {
                            //전처리 & 전송
                            if(file==null)
                                break;
                            /*String sendStringPath =  Environment.getExternalStorageDirectory().toString();
                            fileName = "btimage.jpg"; //임의 지정
                            File file = new File(sendStringPath,fileName);*/

                            ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            image.compress(Bitmap.CompressFormat.JPEG, BTService.IMAGE_QUALITY, compressedImageStream);
                            byte[] compressedImage = compressedImageStream.toByteArray();
                            Log.v(TAG, "Compressed image size: " + compressedImage.length);

//                            ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                            imageView.setImageBitmap(image);

                            // Invoke client thread to send
                            Message imageMessage = new Message();
                            imageMessage.obj = compressedImage;
                            BTService.clientThread.incomingHandler.sendMessage(imageMessage);

                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }

                        break;
                    }

                    case MessageType.COULD_NOT_CONNECT: {//전송 불가능한 상태
                        /*todo : 보내려는 파일 db에 저장*/
                        Toast.makeText(getApplicationContext(), "Could not connect to the paired device", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.SENDING_DATA:{
                        break;
                    }

                    case MessageType.DATA_SENT_OK: {
                        Toast.makeText(getApplicationContext(), "Photo was sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(getApplicationContext(), "Photo was sent, but didn't go through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };


        BTService.serverHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MessageType.DATA_RECEIVED: {
//                        if (progressDialog != null) {
//                            progressDialog.dismiss();
//                            progressDialog = null;
//                        }

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap image = BitmapFactory.decodeByteArray(((byte[]) message.obj), 0, ((byte[]) message.obj).length, options);
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date currentTime_1 = new Date();
                        String dateString = formatter.format(currentTime_1);
                        saveBitmaptoJpeg(image, "nirang", BTService.IMAGE_FILE_NAME + dateString);//파일명예시: nr20180606043124.jpg



//                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                        imageView.setImageBitmap(image);
                        break;
                    }

                    case MessageType.DIGEST_DID_NOT_MATCH: {
                        Toast.makeText(getApplicationContext(), "Photo was received, but didn't come through correctly", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    case MessageType.DATA_PROGRESS_UPDATE: { //todo:없애기(우선확인위해남겨둠)
                        // some kind of update
//                        BTService.progressData = (ProgressData) message.obj;
//                        double pctRemaining = 100 - (((double) BTService.progressData.remainingSize / BTService.progressData.totalSize) * 100);
//                        if (progressDialog == null) {
//                            progressDialog = new ProgressDialog(SelectBT2.this);
//                            progressDialog.setMessage("Receiving photo...");
//                            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                            progressDialog.setProgress(0);
//                            progressDialog.setMax(100);
//                            progressDialog.show();
//                        }
//                        progressDialog.setProgress((int) Math.floor(pctRemaining));
                        break;
                    }

                    case MessageType.INVALID_HEADER: {
                        Toast.makeText(getApplicationContext(), "Photo was sent, but the header was formatted incorrectly", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };

        if (BTService.serverThread == null) {
            Log.v(TAG, "Starting server thread.  Able to accept photos.");
            BTService.serverThread = new ServerThread(BTService.adapter, BTService.serverHandler);
            BTService.serverThread.start();
        }

        Log.v(TAG, "Starting client thread");
        if (BTService.clientThread != null) {
            BTService.clientThread.cancel();
        }
        BTService.clientThread = new ClientThread(BTService.device, BTService.clientHandler);
        BTService.clientThread.start();

        resolver = getContentResolver();
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Log.i("DB변경", "DB가 변경됨을 감지했습니다.");
                getAddedFile();
                if(filePaths.size() > 0) {
                    moveFile();
                }
            }
        };

        resolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastDatabaseUpdateTime", (new Date()).getTime()); //현재 시간 찍는다.
        editor.commit();
    }

    public void saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){ //bitmap객체를 jpg파일로 변환해 저장.

        String ex_storage = Environment.getExternalStorageDirectory().toString();
        // Get Absolute Path in External Sdcard

        String foler_name = "/"+folder+"/";
        String file_name = name+".jpg"; //=fname
        String string_path = ex_storage+foler_name;

        File file_path;

        try{
            file_path = new File(string_path); // file
            if(!file_path.isDirectory()) {
                file_path.mkdirs();
            }

            //기연 수정. file 정보 수정
            File nirang_file = new File(file_path, file_name);
            FileOutputStream out = new FileOutputStream(nirang_file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

            ///////////// 앨범 추가 기능 /////////////
            insertAlbum(nirang_file);

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE); //미디어 스캐닝 (갤러리에 앨범 띄워주기 위해)
            intent.setData(Uri.fromFile(nirang_file));
            sendBroadcast(intent);
            out.flush();
            out.close();
        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
    }

    void getAddedFile(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastDatabaseUpdateTime = preferences.getLong("lastDatabaseUpdateTime", 0); //전에 추가됐던 시간 가져옴
        long newDatabaseUpdateTime = (new Date()).getTime(); //이미지 추가된 시간 저장

        String[] projection = { MediaStore.Images.Media.DATA };

        String where = MediaStore.MediaColumns.DATE_ADDED + ">" + (lastDatabaseUpdateTime/1000); //조건 : 전에 추가됐던 시간 이후에 추가 된 사진

        Cursor imageCursor = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, null);


        filePaths = new ArrayList<>();
        uris = new ArrayList<>(imageCursor.getCount());

        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else {
            int count = imageCursor.getCount();
            if (count > 0 && imageCursor.moveToFirst()) {
                do {
                    String filePath = imageCursor.getString(dataColumnIndex);
                    if (!filePath.contains("nerang")) {
                        if(!filePath.contains("nirang")) {
                            filePaths.add(filePath);
                            Uri imageUri = Uri.parse(filePath);
                            uris.add(imageUri);

//                            // DB에 추가
//                            MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
//                            Picture picture = new Picture(imageUri, pathNow);
//                            dbHandler.addPicture(picture);
                        }
                    }
                } while(imageCursor.moveToNext());
            }
        }

        imageCursor.close();

//If no exceptions, then save the new timestamp
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastDatabaseUpdateTime", newDatabaseUpdateTime); //새로 찍는다.
        editor.commit();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    void moveFile(){

        //내랑 폴더 생성
//                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+ "/Camera/nerang";
        String root = Environment.getExternalStorageDirectory().toString()+ "/nerang"; //영민 변경

        File myDir = new File(root);
        if(!myDir.exists()){
            myDir.mkdirs();
        }

        String filePath;
        Bitmap picture_bitmap;
        Uri uri;
        for(int i=0;i<filePaths.size();i++){
            filePath = filePaths.get(i);
            uri = uris.get(i);

            //사진 복사본 파일 저장
            String image_name = String.valueOf(System.currentTimeMillis());
            String fname = "Image-" + image_name + ".jpg"; //이미지 이름
            file = new File(myDir, fname);
            fileName = fname;
            System.out.println(file.getAbsolutePath()); //로그캣 확인
            // if (file.exists()) file.delete();
            Log.i("LOAD", root + fname); //복사본 저장 확인
            try { // 앨범에 이미지 복사본 저장
                FileOutputStream out = new FileOutputStream(file);
                picture_bitmap = BitmapFactory.decodeFile(filePath);
                picture_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); //영민 jpg로 변경
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ///////////// 앨범 추가 기능 /////////////
            String uString = getImageContentUri(this, file).toString();
            String imageId = uString.substring(uString.lastIndexOf("/")+1);
            System.out.println(imageId);
            Uri thumbnailUri = uriToThumbnail(imageId);
            Photo photo = new Photo(thumbnailUri, Uri.parse(file.getAbsolutePath()));
            MyApplication myapp = (MyApplication)getApplication();
            if (myapp.getAlbum_title() != null) {
                newProduct(photo);
            }
//
//
            //자동전송 기연추가
            for (BluetoothDevice device : BTService.adapter.getBondedDevices()) {
                if (device.equals(BTService.device)) { //서비스에서 받아오도록했음
                    Log.v(TAG, "Starting client thread");
                    if (BTService.clientThread != null) {
                        BTService.clientThread.cancel();
                    }
                    BTService.clientThread = new ClientThread(device, BTService.clientHandler);
                    BTService.clientThread.start();
                }
            }


            //앨범 삭제
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Images.Media.DATA + " = ?";
            String[] selectionArgs = {filePath}; // 실제 파일의 경로
            resolver.delete(images, selection, selectionArgs);


            //원본 파일 삭제
            File file_delete = new File(uri.getPath());
            if(file_delete.exists()){
                file_delete.delete();
            }
        }
    }

    ///////////// 앨범 추가 기능 /////////////
    private void insertAlbum(File file1) {
        String bUri = getImageContentUri(this, file1).toString();
        System.out.println(bUri);
        String imageId = bUri.substring(bUri.lastIndexOf("/")+1);
        System.out.println(imageId);
        Uri thumbnailUri = uriToThumbnail(imageId);
        Photo photo = new Photo(thumbnailUri, Uri.parse(file1.getAbsolutePath()));
        MyApplication myapp = (MyApplication)getApplication();
        if (myapp.getAlbum_title() != null) {
            System.out.println(myapp.getAlbum_title());
            newProduct(photo);
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private Uri uriToThumbnail(String imageId) {
        String[] projection = {MediaStore.Images.Thumbnails.DATA};

        Cursor thumbnailCursor = contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // Which columns to return
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{imageId},
                null);

        if (thumbnailCursor.moveToFirst()) {
            int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);
            // Generate a tiny thumbnail version.
            String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
            thumbnailCursor.close();
            return Uri.parse(thumbnailPath);
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
            thumbnailCursor.close();
            return uriToThumbnail(imageId);
        }
    }

    public void newProduct(Photo photo) { // insert
        AlbumDBHandler dbHandler = new AlbumDBHandler(this, null, null, 1);
        MyApplication myapp = (MyApplication)getApplication();
        Album product = new Album(myapp.getAlbum_title(), photo);
        dbHandler.addProduct(product);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        BTService.clientThread = null;
        BTService.serverThread = null;
        super.onDestroy();
    }
}
