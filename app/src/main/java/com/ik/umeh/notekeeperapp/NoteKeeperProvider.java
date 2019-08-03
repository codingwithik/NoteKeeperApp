package com.ik.umeh.notekeeperapp;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class NoteKeeperProvider extends ContentProvider {

    private static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    private NoteKeeperOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    public static final int NOTES_EXPANDED = 2;
    public static final int NOTES_ROW = 3;
    private static final int COURSES_ROW = 4;
    private static final int NOTES_EXPANDED_ROW = 5;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH + "/#", COURSES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH + "/#", NOTES_ROW);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED + "/#", NOTES_EXPANDED_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.delete(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.delete(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case COURSES:
                // vnd.android.cursor.dir/vnd.com.jwhh.jim.notekeeper.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NoteKeeperProviderContract.Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
                break;
            case COURSES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Courses.PATH;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH;
                break;
            case NOTES_EXPANDED_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + NoteKeeperProviderContract.Notes.PATH_EXPANDED;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case NOTES:
                rowId = db.insert(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, null, values);
                // content://com.ik.umeh.notekeeperapp.provider/notes/1
                rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(NoteKeeperProviderContract.Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
                break;
        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                cursor = db.query(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);

                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);

                break;
            case NOTES_EXPANDED_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.NoteInfoEntry.getQName(NoteKeeperDatabaseContract.NoteInfoEntry._ID) + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = notesExpandedQuery(db, projection, rowSelection, rowSelectionArgs, null);
                break;
        }
        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for(int idx=0; idx < projection.length; idx++) {
            columns[idx] = projection[idx].equals(BaseColumns._ID) ||
                    projection[idx].equals(NoteKeeperProviderContract.CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteKeeperDatabaseContract.NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }

        String tablesWithJoin = NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME + " JOIN " +
                NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME + " ON " +
                NoteKeeperDatabaseContract.NoteInfoEntry.getQName(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                NoteKeeperDatabaseContract.CourseInfoEntry.getQName(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_ID);

        return db.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.update(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.update(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteKeeperDatabaseContract.NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXPANDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }
}


















