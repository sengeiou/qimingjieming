package com.tjyw.qmjmqd.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.tjyw.atom.network.conf.ISection;
import com.tjyw.atom.network.param.ListRequestParam;
import com.tjyw.atom.network.utils.DateTimeUtils;
import com.tjyw.qmjmqd.ClientQmjmApplication;
import com.tjyw.qmjmqd.R;
import com.tjyw.qmjmqd.activity.BaseActivity;
import com.tjyw.qmjmqd.activity.ClientMasterActivity;
import com.tjyw.qmjmqd.factory.IClientActivityLaunchFactory;
import com.xhinliang.lunarcalendar.LunarCalendar;

import java.util.Calendar;

import atom.pub.fragment.AtomPubBaseFragment;
import atom.pub.inject.From;
import atom.pub.interfaces.AtomPubValidationListener;

/**
 * Created by stephen on 07/08/2017.
 */
public class ClientMasterExplainFragment extends AtomPubBaseFragment implements ClientGregorianFragment.OnGregorianSelectedListener {

    @From(R.id.nGenderMale)
    protected ViewGroup nGenderMale;

    @From(R.id.nGenderFemale)
    protected ViewGroup nGenderFemale;

    @Order(1)
    @Pattern(regex = "^[\\u4e00-\\u9fa5]{1,2}$", messageResId = R.string.atom_pub_resStringNameInputHint)
    @From(R.id.nSurname)
    protected EditText nSurname;

    @Order(2)
    @Pattern(regex = "^[\\u4e00-\\u9fa5]{1,2}$", messageResId = R.string.atom_pub_resStringNameInputHint)
    @From(R.id.nGivenName)
    protected EditText nGivenName;

    @Order(3)
    @Length(min = 1, messageResId = R.string.atom_pub_resStringDateOfBirthHint)
    @From(R.id.nDateOfBirth)
    protected TextView nDateOfBirth;

    @From(R.id.atom_pub_resIdsOK)
    protected TextView atom_pub_resIdsOK;

    protected ListRequestParam listRequestParam;

    protected Validator validator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        listRequestParam = new ListRequestParam();
        return inflater.inflate(R.layout.atom_master_explain, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        validator = new Validator(this);
        validator.setValidationListener(new AtomPubValidationListener(ClientQmjmApplication.getContext()) {

            @Override
            public void onValidationSucceeded() {
                listRequestParam.surname = nSurname.getText().toString();
                listRequestParam.name = nGivenName.getText().toString();

                IClientActivityLaunchFactory.launchExplainMasterActivity(
                        (BaseActivity) getActivity(), listRequestParam, 1000
                );
            }
        });

        nGivenName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        validator.validate();
                    default:
                        return false;
                }
            }
        });

        Calendar calendar = DateTimeUtils.getCurrentDateTime();
        if (null != calendar) {
            listRequestParam.day = DateTimeUtils.printCalendarByPattern(calendar, DateTimeUtils.yyyy_MM_dd_HH);
            nDateOfBirth.setText(DateTimeUtils.printCalendarByPattern(calendar, ClientQmjmApplication.pGetString(R.string.atom_pub_resStringDateSolar)));
        }

        nGenderMale.setSelected(true);

        nGenderMale.setOnClickListener(this);
        nGenderFemale.setOnClickListener(this);
        nDateOfBirth.setOnClickListener(this);
        atom_pub_resIdsOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nGenderMale:
                listRequestParam.gender = ISection.GENDER.MALE;
                v.setSelected(true);
                nGenderFemale.setSelected(false);
                break ;
            case R.id.nGenderFemale:
                listRequestParam.gender = ISection.GENDER.FEMALE;
                v.setSelected(true);
                nGenderMale.setSelected(false);
                break ;
            case R.id.nDateOfBirth:
                ((ClientMasterActivity) getActivity()).showGregorianFragment(this);
                break ;
            case R.id.atom_pub_resIdsOK:
                validator.validate();
        }
    }

    @Override
    public void gregorianOnSelected(LunarCalendar lunarCalendar, boolean isGregorianSolar, String hour, int postHour) {
        Calendar calendar = DateTimeUtils.getCalendar(lunarCalendar.getDate());
        if (null != calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, postHour);
            listRequestParam.day = DateTimeUtils.printCalendarByPattern(calendar, DateTimeUtils.yyyy_MM_dd_HH);
        }

        if (isGregorianSolar) {
            nDateOfBirth.setText(DateTimeUtils.printCalendarByPattern(calendar, ClientQmjmApplication.pGetString(R.string.atom_pub_resStringDateSolar)));
        } else {
            nDateOfBirth.setText(
                    ClientQmjmApplication.pGetString(
                            R.string.atom_pub_resStringDateLunar,
                            lunarCalendar.getLunarYear(),
                            lunarCalendar.getLunarMonth(),
                            lunarCalendar.getLunarDay(),
                            hour
                    )
            );
        }
    }
}