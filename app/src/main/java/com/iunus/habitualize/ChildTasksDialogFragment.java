package com.iunus.habitualize;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ListView;

import java.util.List;


public class ChildTasksDialogFragment extends DialogFragment {

    private Context context;
    private View mainView;
    private int habitId;
    private List<Task> tasks;

    public ChildTasksDialogFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ChildTasksDialogFragment newInstance(int habitId) {
        ChildTasksDialogFragment fragment = new ChildTasksDialogFragment();
        Bundle args = new Bundle();
        args.putInt("habitId", habitId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getBaseContext();

        Bundle args = getArguments();

        habitId = args.getInt("habitId");

        DBHelper dbHelper = new DBHelper(context);
        tasks = dbHelper.getTasksOfHabit(habitId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_child_tasks_dialog, container, false);

        String title = tasks.get(0).getName();
        getDialog().setTitle(title);

        setUpListView();

        Button button = (Button) mainView.findViewById(R.id.negative_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return mainView;
    }

    private void setUpListView() {
        ListView listView = (ListView) mainView.findViewById(R.id.child_tasks_view);
        ChildTaskListAdapter adapter = new ChildTaskListAdapter(context, tasks);
        listView.setAdapter(adapter);
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
