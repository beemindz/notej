package com.beemindz.notej.activity.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.beemindz.notej.R;
import com.beemindz.notej.util.CommonUtils;

import java.util.Calendar;

/**
 * Created by Sony on 8/10/2014.
 */
public class DueDateDialogFragment extends DialogFragment {
  public static Calendar mCalendar;
  DatePicker datePicker;
  Button btnDialogSave, btnDialogCancel;

  public static DueDateDialogFragment newInstance(Calendar calendar) {
    DueDateDialogFragment dueDateDialogFragment = new DueDateDialogFragment();
    mCalendar = calendar;
    return dueDateDialogFragment;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle(R.string.dialog_choose_due_date_title);
    return dialog;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_dialog_due_date, container, false);
    datePicker = (DatePicker) view.findViewById(R.id.datePicker1);
    datePicker.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH), null);

    btnDialogSave = (Button) view.findViewById(R.id.btnDialogSave);
    btnDialogSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mCalendar.set(Calendar.YEAR, datePicker.getYear());
        mCalendar.set(Calendar.MONTH, datePicker.getMonth());
        mCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
        TextView tvDueDate = (TextView) getActivity().findViewById(R.id.tvDueDate);
        tvDueDate.setText(CommonUtils.getStringDate(mCalendar, CommonUtils.getDateFormatSystem(getActivity())));

        dismiss();
      }
    });

    btnDialogCancel = (Button) view.findViewById(R.id.btnDialogCancel);
    btnDialogCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextView tvDueDate = (TextView) getActivity().findViewById(R.id.tvDueDate);
        tvDueDate.setText("");
        dismiss();
      }
    });
    return view;
  }

}
