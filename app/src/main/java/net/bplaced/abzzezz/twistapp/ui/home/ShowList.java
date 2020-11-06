/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import id.ionbit.ionalert.IonAlert;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.util.adapters.ShowAdapter;
import net.bplaced.abzzezz.twistapp.util.misc.InputDialogBuilder;

public class ShowList extends Fragment {

    private ShowAdapter showAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_list, container, false);

        this.showAdapter = new ShowAdapter(TwistAppMain.getInstance().getShowSaver().getList(), getContext());
        final ListView showList = root.findViewById(R.id.show_list_view);
        showList.setAdapter(showAdapter);

        showList.setOnItemClickListener((adapterView, view, i, l) -> {
            final Intent intent = new Intent(getActivity(), SelectedShow.class);
            intent.putExtra("show_index", i);
            startActivity(intent);
            getActivity().finish();
        });

        showList.setOnItemLongClickListener((adapterView, view, i, l) -> {
            new IonAlert(getContext()).setTitleText("Remove show?").setContentText("Do you want to remove the show?").setConfirmText("Yes, remove").setCancelText("Cancel").setConfirmClickListener(ionAlert -> {
                TwistAppMain.getInstance().getShowSaver().removeShow(i);
                ionAlert.dismissWithAnimation();
                showAdapter.notifyDataSetChanged();
            }).setCancelClickListener(IonAlert::dismissWithAnimation).show();
            return true;
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FloatingActionButton addItemButton = view.findViewById(R.id.add_show_button);
        addItemButton.setOnClickListener(view1 -> new InputDialogBuilder(new InputDialogBuilder.InputDialogListener() {
            @Override
            public void onDialogInput(String text) {
                showAdapter.addItem(text);
            }

            @Override
            public void onDialogDenied() {
            }
        }).showInput("Add item", "Please input url to add", getContext()));
    }
}
