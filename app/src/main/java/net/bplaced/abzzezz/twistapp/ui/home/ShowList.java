/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import id.ionbit.ionalert.IonAlert;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.util.InputDialogBuilder;
import net.bplaced.abzzezz.twistapp.util.file.ShowSaver;
import net.bplaced.abzzezz.twistapp.util.tasks.AddItemTask;
import net.bplaced.abzzezz.twistapp.util.tasks.RefreshTask;
import net.bplaced.abzzezz.twistapp.util.tasks.TaskExecutor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ShowList extends Fragment {

    private ShowAdapter showAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_list, container, false);

        this.showAdapter = new ShowAdapter(TwistAppMain.getINSTANCE().getShowSaver().getList(), getContext());
        final ListView showList = root.findViewById(R.id.show_list_view);
        showList.setAdapter(showAdapter);

        showList.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(getActivity(), SelectedShow.class);
            intent.putExtra("show_index", i);
            startActivity(intent);
            getActivity().finish();
        });

        showList.setOnItemLongClickListener((adapterView, view, i, l) -> {
            new IonAlert(getContext()).setTitleText("Remove show?").setContentText("Do you want to remove the show?").setConfirmText("Yes, remove").setCancelText("Cancel").setConfirmClickListener(ionAlert -> {
                TwistAppMain.getINSTANCE().getShowSaver().removeShow(i);
                ionAlert.dismissWithAnimation();
                showAdapter.notifyDataSetChanged();
            }).setCancelClickListener(IonAlert::dismissWithAnimation).show();
            return true;
        });

        /*
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

         */

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FloatingActionButton addItemButton = view.findViewById(R.id.add_show_button);
        addItemButton.setOnClickListener(view1 -> {
            InputDialogBuilder urlDialog = new InputDialogBuilder(new InputDialogBuilder.InputDialogListener() {
                @Override
                public void onDialogInput(String text) {
                    showAdapter.addItem(text);
                }

                @Override
                public void onDialogDenied() {
                }
            });

            urlDialog.showInput("Add item", "Please input url to add", getContext());
        });
    }

    public class ShowAdapter extends BaseAdapter {
        private final Context context;
        private final ShowSaver showSaver;
        private final List<String> shows;

        public ShowAdapter(List<String> shows, Context context) {
            this.context = context;
            this.showSaver = TwistAppMain.getINSTANCE().getShowSaver();
            this.shows = shows;
        }

        @Override
        public int getCount() {
            return showSaver.getList().size();
        }

        @Override
        public Object getItem(int i) {
            return shows.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public JSONObject getDetails(final int index) {
            try {
                return new JSONObject(shows.get(index));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public View getView(int pos, View view, ViewGroup viewGroup) {
            if (view == null) view = LayoutInflater.from(context).inflate(R.layout.show_layout, viewGroup, false);
            final JSONObject details = getDetails(pos);
            final TextView showName = view.findViewById(R.id.show_name);
            final TextView episodes = view.findViewById(R.id.show_episodes);
            final ImageView refreshButton = view.findViewById(R.id.refresh_button);

            refreshButton.setOnClickListener(view1 -> new RefreshTask(pos).executeAsync(new TaskExecutor.Callback<String>() {
                @Override
                public void preExecute() {

                }

                @Override
                public void onComplete(String result) {
                    Toast.makeText(context, "Refreshed. Episodes fetched: " + result, Toast.LENGTH_SHORT).show();
                }
            }));

            try {
                showName.setText(details.getString("title"));
                episodes.append(details.getString("episode"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return view;
        }

        public void addItem(final String item) {
            new AddItemTask(item).executeAsync(new TaskExecutor.Callback<String[]>() {
                @Override
                public void preExecute() {
                }

                @Override
                public void onComplete(String[] result) {
                    try {
                        JSONObject detailJSON = new JSONObject(result[0]);
                        JSONObject infJSON = new JSONObject();
                        infJSON.put("url", result[2]);
                        infJSON.put("title", detailJSON.getString("title"));
                        infJSON.put("episode", detailJSON.getJSONArray("episodes").length());
                        infJSON.put("description", detailJSON.get("description"));
                        if (TwistAppMain.getINSTANCE().getShowSaver().containsShow(detailJSON.getString("id")) && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("check_existing", false)) {
                            Toast.makeText(context, "The show, you wanted to add, already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        infJSON.put("id", detailJSON.getString("id"));
                        infJSON.put("episodes", new JSONObject(result[1]));
                        shows.add(infJSON.toString());
                        TwistAppMain.getINSTANCE().getShowSaver().addShow(infJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}
