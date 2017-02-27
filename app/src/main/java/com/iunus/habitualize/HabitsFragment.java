package com.iunus.habitualize;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Iunus on 08/02/2017.
 */
public class HabitsFragment extends Fragment {

    public final static int REQUEST_EDIT_HABIT = 2;

    private List<Habit> habits;
    private DBHelper dbHelper;

    private View mView;
    private ListView listView;

    private ActionMode actionMode;
    private List<Integer> selectedItemsIdx;

    public static HabitsFragment newInstance() {

        Bundle args = new Bundle();

        HabitsFragment fragment = new HabitsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DBHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_habits, container, false);

        setUpListView();

        return mView;
    }
    
    private void setUpListView() {
        selectedItemsIdx = new ArrayList<>();
        listView = (ListView) mView.findViewById(R.id.listView_habits);

        setListView();

        listView.setOnItemClickListener(itemClickListener);

        setUpItemLongClick();
    }

    void setListView() {
        habits = dbHelper.getHabits();
        HabitListAdapter adapter = new HabitListAdapter(getContext(), habits);
        listView.setAdapter(adapter);
    }

    final ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showChildTasksDialog(position);
        }
    };

    private void showChildTasksDialog(int idx) {
        int id = habits.get(idx).getId();

        FragmentManager fm = getFragmentManager();
        ChildTasksDialogFragment fragment = ChildTasksDialogFragment.newInstance(id);
        fragment.setTargetFragment(this, 0);
        fragment.show(fm, "fragment_child_task");
    }


    /******* Methods handling long clicks on list items and subsequent action mode ****************/

    private void setUpItemLongClick() {
        ListView taskListView = (ListView) mView.findViewById(R.id.listView_habits);

        taskListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        taskListView.setMultiChoiceModeListener(multiChoiceModeListener);

        selectedItemsIdx = new ArrayList<>();
    }

    final AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
            if (checked) {
                selectedItemsIdx.add(position);
                if (selectedItemsIdx.size() > 1) {
                    mode.getMenu().findItem(R.id.action_edit_task).setVisible(false);
                }
            } else {
                selectedItemsIdx.remove(selectedItemsIdx.indexOf(position));
                if (selectedItemsIdx.size() <= 1) {
                    mode.getMenu().findItem(R.id.action_edit_task).setVisible(true);
                }
            }

            mode.setTitle(String.format(Locale.ENGLISH, "%d Item(s) Selected", selectedItemsIdx.size()));
        }

        @Override
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);

            selectedItemsIdx.clear();

            actionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return false;
        }


        @Override
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit_task:
                    mode.finish();
                    editHabit();
                    return true;
                case R.id.action_delete_task:
                    mode.finish();
                    deleteSelectedHabits();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(android.view.ActionMode mode) {

        }
    };

    private void deleteSelectedHabits() {
        for (int i : selectedItemsIdx) {
            int id = habits.get(i).getId();
            dbHelper.deleteHabit(id);
        }

        ((MainActivity) getActivity()).notifyDataSetChanged();
        Toast.makeText(getContext(), "Task(s) Deleted", Toast.LENGTH_LONG).show();
    }

    private void editHabit() {
        if (selectedItemsIdx.size() != 1) {
            return;
        }

        int pos = selectedItemsIdx.get(0);
        Habit habit = habits.get(pos);
        Intent intent = new Intent(getContext(), HabitFormActivity.class);
        intent.putExtra("requestCode", REQUEST_EDIT_HABIT);
        intent.putExtra("habit", habit);
        startActivityForResult(intent, REQUEST_EDIT_HABIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_HABIT) {
            setUpListView();
            ((MainActivity)getActivity()).notifyDataSetChanged();
            Toast.makeText(getContext(), "Task Updated", Toast.LENGTH_LONG).show();
        }
    }

    public void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }


    private View getListViewItemByIndex(int idx, ListView listView) {
        final int firstItemPosition = listView.getFirstVisiblePosition();
        final int lastItemPosition = firstItemPosition + listView.getChildCount() - 1;

        if (idx < firstItemPosition || idx > lastItemPosition) {
            return listView.getAdapter().getView(idx, null, listView);
        } else {
            final int childIdx = idx - firstItemPosition;
            return listView.getChildAt(childIdx);
        }
    }
}
