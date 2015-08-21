package info.androidhive.tabsswipe;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class ContactsFragment extends Fragment {

	private static final String TAG = ContactsFragment.class.getSimpleName();
	private static final int REQUEST_CODE_PICK_CONTACTS = 1;
	private Uri uriContact;
	private String contactID;     // contacts unique ID
	private TextView contentFullName;
	private TextView contentContactTel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

		return rootView;
	}

	public void onClickSelectContact(View btnSelectContact) {

		// using native contacts selection
		// Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
		startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == getActivity().RESULT_OK) {
			Log.d(TAG, "Response: " + data.toString());
			uriContact = data.getData();

			retrieveContactName();
			retrieveContactNumber();
			retrieveContactPhoto();

		}
	}

	private void retrieveContactPhoto() {

		Bitmap photo = null;

//        try {
		InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getActivity().getContentResolver(),
				ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

		if (inputStream != null) {
			photo = BitmapFactory.decodeStream(inputStream);
			ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_contact);
			imageView.setImageBitmap(photo);
		} else {
			Log.d(TAG, "Image not found, default will be used");
			Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_profile", "drawable", getActivity().getPackageName()));
			ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_contact);
			imageView.setImageDrawable(drawable);
		}

//            assert inputStream != null;
//            inputStream.close();

//        } catch (IOException e) {
//            e.printStackTrace();
//        }

	}

	private void retrieveContactNumber() {

		String contactNumber = null;

		// getting contacts ID
		Cursor cursorID = getActivity().getContentResolver().query(uriContact,
				new String[]{ContactsContract.Contacts._ID},
				null, null, null);

		if (cursorID.moveToFirst()) {

			contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
		}

		cursorID.close();

		Log.d(TAG, "Contact ID: " + contactID);

		// Using the contact ID now we will get contact phone number
		Cursor cursorPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
						ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
						ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

				new String[]{contactID},
				null);

		if (cursorPhone.moveToFirst()) {
			contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		}

		cursorPhone.close();

		Log.d(TAG, "Contact Phone Number: " + contactNumber);

		if(contactNumber != null) {
			contentContactTel.setText(contactNumber);
		} else {
			contentContactTel.setText("Number not found");
		}
	}

	private void retrieveContactName() {

		String contactName = null;

		// querying contact data store
		Cursor cursor = getActivity().getContentResolver().query(uriContact, null, null, null, null);

		if (cursor.moveToFirst()) {

			// DISPLAY_NAME = The display name for the contact.
			// HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

			contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		}

		cursor.close();

		Log.d(TAG, "Contact Name: " + contactName);

		if(contactName != null) {
			contentFullName.setText(contactName);
		} else {
			contentFullName.setText("Name not found, please select another contact");
		}
	}
}
