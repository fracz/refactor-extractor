/**
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thoughtcrime.securesms;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.thoughtcrime.securesms.contacts.ContactAccessor;
import org.thoughtcrime.securesms.contacts.ContactAccessor.ContactData;
import org.thoughtcrime.securesms.contacts.ContactAccessor.NumberData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Activity for selecting a list of contacts.  Displayed inside
 * a PushContactSelectionActivity tab frame, and ultimately called by
 * ComposeMessageActivity for selecting a list of destination contacts.
 *
 * @author Moxie Marlinspike
 *
 */

public class PushContactSelectionListFragment extends SherlockListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>
{
  private final int STYLE_ATTRIBUTES[] = new int[]{R.attr.contact_selection_push_user,
                                                   R.attr.contact_selection_lay_user,
                                                   R.attr.contact_selection_push_label,
                                                   R.attr.contact_selection_lay_label};

  private final HashMap<Long, ContactData> selectedContacts = new HashMap<Long, ContactData>();
  private static LayoutInflater li;
  private        TypedArray     drawables;


  @Override
  public void onActivityCreated(Bundle icicle) {
    super.onCreate(icicle);
    li = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    initializeResources();
    initializeCursor();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.push_contact_selection_list_activity, container, false);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.contact_selection_list, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
    case R.id.menu_select_all:
      handleSelectAll();
      return true;
    case R.id.menu_unselect_all:
      handleUnselectAll();
      return true;
    }

    super.onOptionsItemSelected(item);
    return false;
  }

  public List<ContactData> getSelectedContacts() {
    List<ContactData> selected = new LinkedList<ContactData>();
    selected.addAll(selectedContacts.values());

    return selected;
  }

  private void handleUnselectAll() {
    selectedContacts.clear();
    ((CursorAdapter) getListView().getAdapter()).notifyDataSetChanged();
  }

  private void handleSelectAll() {
    selectedContacts.clear();

    Cursor cursor = null;

    try {
      cursor = ContactAccessor.getInstance().getCursorForContactsWithNumbers(getActivity());

      while (cursor != null && cursor.moveToNext()) {
        ContactData contactData = ContactAccessor.getInstance().getContactData(getActivity(), cursor);

        if (contactData.numbers.isEmpty()) continue;
        else if (contactData.numbers.size() == 1) addSingleNumberContact(contactData);
        else addMultipleNumberContact(contactData, null, null);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }

    ((CursorAdapter) getListView().getAdapter()).notifyDataSetChanged();
  }

  private void addSingleNumberContact(ContactData contactData) {
    selectedContacts.put(contactData.id, contactData);
  }

  private void removeContact(ContactData contactData) {
    selectedContacts.remove(contactData.id);
  }

  private void addMultipleNumberContact(ContactData contactData, TextView textView, CheckBox checkBox) {
    String[] options = new String[contactData.numbers.size()];
    int i            = 0;

    for (NumberData option : contactData.numbers) {
      options[i++] = option.type + " " + option.number;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getString(R.string.ContactSelectionlistFragment_select_for) + " " + contactData.name);
    builder.setMultiChoiceItems(options, null, new DiscriminatorClickedListener(contactData));
    builder.setPositiveButton(android.R.string.ok, new DiscriminatorFinishedListener(contactData, textView, checkBox));
    builder.setOnCancelListener(new DiscriminatorFinishedListener(contactData, textView, checkBox));
    builder.show();
  }

  private void initializeCursor() {
    setListAdapter(new ContactSelectionListAdapter(getActivity(), null));
    this.getLoaderManager().initLoader(0, null, this);
  }

  private void initializeResources() {
    this.getListView().setFocusable(true);
    this.drawables = getActivity().obtainStyledAttributes(STYLE_ATTRIBUTES);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    ((ContactItemView)v).selected();
  }

  private class ContactSelectionListAdapter extends CursorAdapter {

    public ContactSelectionListAdapter(Context context, Cursor c) {
      super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
      ContactItemView view = new ContactItemView(context);
      bindView(view, context, cursor);

      return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
      boolean isPushUser;
      try {
        isPushUser = (cursor.getInt(cursor.getColumnIndexOrThrow(ContactAccessor.PUSH_COLUMN)) > 0);
      } catch (IllegalArgumentException iae) {
        isPushUser = false;
      }
      ContactData contactData = ContactAccessor.getInstance().getContactData(context, cursor);
      PushContactData pushContactData = new PushContactData(contactData, isPushUser);
      ((ContactItemView)view).set(pushContactData);
    }
  }

  private class PushContactData {
    private final ContactData contactData;
    private final boolean     pushSupport;
    public PushContactData(ContactData contactData, boolean pushSupport) {
      this.contactData = contactData;
      this.pushSupport = pushSupport;
    }
  }

  private class ContactItemView extends RelativeLayout {
    private ContactData contactData;
    private boolean     pushSupport;
    private CheckBox    checkBox;
    private TextView    name;
    private TextView    number;
    private TextView    label;
    private View        pushLabel;

    public ContactItemView(Context context) {
      super(context);

      li.inflate(R.layout.push_contact_selection_list_item, this, true);

      this.name = (TextView) findViewById(R.id.name);
      this.number = (TextView) findViewById(R.id.number);
      this.label = (TextView) findViewById(R.id.label);
      this.checkBox = (CheckBox) findViewById(R.id.check_box);
      this.pushLabel = findViewById(R.id.push_support_label);
    }

    public void selected() {

      checkBox.toggle();

      if (checkBox.isChecked()) {
        if (contactData.numbers.size() == 1) addSingleNumberContact(contactData);
        else addMultipleNumberContact(contactData, name, checkBox);
      } else {
        removeContact(contactData);
      }
    }

    public void set(PushContactData pushContactData) {
      this.contactData = pushContactData.contactData;
      this.pushSupport = pushContactData.pushSupport;

      if (!pushSupport) {
        this.name.setTextColor(drawables.getColor(1, 0xff000000));
        this.number.setTextColor(drawables.getColor(1, 0xff000000));
        this.pushLabel.setBackgroundColor(drawables.getColor(3, 0x99000000));
      } else {
        this.name.setTextColor(drawables.getColor(0, 0xa0000000));
        this.number.setTextColor(drawables.getColor(0, 0xa0000000));
        this.pushLabel.setBackgroundColor(drawables.getColor(2, 0xff64a926));
      }

      if (selectedContacts.containsKey(contactData.id))
        this.checkBox.setChecked(true);
      else
        this.checkBox.setChecked(false);

      this.name.setText(contactData.name);

      if (contactData.numbers.isEmpty()) {
        this.name.setEnabled(false);
        this.number.setText("");
        this.label.setText("");
      } else {
        this.number.setText(contactData.numbers.get(0).number);
        this.label.setText(contactData.numbers.get(0).type);
      }
    }
  }

  private class DiscriminatorFinishedListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private final ContactData contactData;
    private final TextView    textView;
    private final CheckBox    checkBox;

    public DiscriminatorFinishedListener(ContactData contactData, TextView textView, CheckBox checkBox) {
      this.contactData = contactData;
      this.textView = textView;
      this.checkBox = checkBox;
    }

    public void onClick(DialogInterface dialog, int which) {
      ContactData selected = selectedContacts.get(contactData.id);

      if (selected == null && textView != null) {
        if (textView != null) checkBox.setChecked(false);
      } else if (selected.numbers.size() == 0) {
        selectedContacts.remove(selected.id);
        if (textView != null) checkBox.setChecked(false);
      }

      if (textView == null)
        ((CursorAdapter) getListView().getAdapter()).notifyDataSetChanged();
    }

    public void onCancel(DialogInterface dialog) {
      onClick(dialog, 0);
    }
  }

  private class DiscriminatorClickedListener implements DialogInterface.OnMultiChoiceClickListener {
    private final ContactData contactData;

    public DiscriminatorClickedListener(ContactData contactData) {
      this.contactData = contactData;
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
      Log.w("ContactSelectionListActivity", "Got checked: " + isChecked);

      ContactData existing = selectedContacts.get(contactData.id);

      if (existing == null) {
        Log.w("ContactSelectionListActivity", "No existing contact data, creating...");

        if (!isChecked)
          throw new AssertionError("We shouldn't be unchecking data that doesn't exist.");

        existing = new ContactData(contactData.id, contactData.name);
        selectedContacts.put(existing.id, existing);
      }

      NumberData selectedData = contactData.numbers.get(which);

      if (!isChecked) existing.numbers.remove(selectedData);
      else existing.numbers.add(selectedData);
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
    return ContactAccessor.getInstance().getCursorLoaderForContactsWithNumbers(getActivity());
  }

  @Override
  public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
    Cursor pushCursor = ContactAccessor.getInstance().getCursorForContactsWithPush(getActivity());
    ((CursorAdapter) getListAdapter()).changeCursor(new MergeCursor(new Cursor[]{pushCursor,cursor}));
    ((TextView)getView().findViewById(android.R.id.empty)).setText(R.string.contact_selection_group_activity__no_contacts);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    ((CursorAdapter) getListAdapter()).changeCursor(null);
  }
}