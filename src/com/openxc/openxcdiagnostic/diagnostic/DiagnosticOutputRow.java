package com.openxc.openxcdiagnostic.diagnostic;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.VehicleMessage;
import com.openxc.openxcdiagnostic.R;
import com.openxc.openxcdiagnostic.util.Utilities;

public class DiagnosticOutputRow {

    private LinearLayout mView;
    private DiagnosticOutputTable mTable;
    private VehicleMessage mResponse;
    private VehicleMessage mRequest;

    public DiagnosticOutputRow(DiagnosticActivity context,
            DiagnosticOutputTable table, VehicleMessage req,
            VehicleMessage resp) {

        mView = (LinearLayout) context.getLayoutInflater().inflate(R.layout.diagoutputrow, null);
        mTable = table;
        mResponse = resp;
        mRequest = req;

        initButtons(context, req, resp);
        fillOutputResponseTable(context, resp);
        String timestampString;
        if (resp.getTimestamp() != null) {
            timestampString = Utilities.epochTimeToTime(resp.getTimestamp());
        } else {
            timestampString = "0:00";
        }
        ((TextView) mView.findViewById(R.id.outputRowTimestamp)).setText(timestampString);
    }

    private void initButtons(final DiagnosticActivity context,
            final VehicleMessage req, final VehicleMessage resp) {

        Button detailsButton = (Button) mView.findViewById(R.id.outputMoreButton);
        detailsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DiagnosticResponseDetailsAlertManager.show(context, req, resp);
            }
        });

        final Button deleteButton = (Button) mView.findViewById(R.id.responseDeleteButton);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTable.removeRow(DiagnosticOutputRow.this);
            }
        });

        Button resendButton = (Button) mView.findViewById(R.id.outputResendButton);
        resendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.send(req);
            }
        });

    }

    private void createAndAddRowToOutput(Activity context, LinearLayout parent,
            String label, String value, VehicleMessage msg) {

        LinearLayout row = (LinearLayout) context.getLayoutInflater().inflate(R.layout.outputresponsetablerow, null);
        ((TextView) row.findViewById(R.id.outputTableRowLabel)).setText(label);

        TextView valueText = (TextView) row.findViewById(R.id.outputTableRowValue);
        valueText.setText(value);
        if (msg instanceof DiagnosticResponse) {
            valueText.setTextColor(Utilities.getOutputColor(context, (DiagnosticResponse) msg));
        }
        parent.addView(row);
    }

    private void fillOutputResponseTable(DiagnosticActivity context,
            VehicleMessage msgResponse) {

        LinearLayout infoTable = (LinearLayout) mView.findViewById(R.id.outputInfo);
            if (msgResponse instanceof DiagnosticResponse) {
                DiagnosticResponse resp = (DiagnosticResponse) msgResponse;
                createAndAddRowToOutput(context, infoTable, "bus", Utilities.getBusOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "id", Utilities.getIdOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "mode", Utilities.getModeOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "pid", Utilities.getPidOutput(resp), resp);
                boolean responseSuccess = resp.isSuccessful();
                createAndAddRowToOutput(context, infoTable, "success", Utilities.getSuccessOutput(resp), resp);
                if (responseSuccess) {
                    fillOutputTableWithSuccessDetails(infoTable, context, resp);
                } else {
                    fillOutputTableWithFailureDetails(infoTable, context, resp);
                }
            } else if (msgResponse instanceof CommandResponse ){
                CommandResponse cmdResponse = (CommandResponse) msgResponse;
                createAndAddRowToOutput(context, infoTable, "command response", 
                        Utilities.getMessageOutput(cmdResponse), cmdResponse);
            }
    }

    private void fillOutputTableWithSuccessDetails(LinearLayout responseTable,
            Activity context, DiagnosticResponse resp) {
        createAndAddRowToOutput(context, responseTable, "payload", Utilities.getPayloadOutput(resp), resp);
        createAndAddRowToOutput(context, responseTable, "value", Utilities.getValueOutput(resp), resp);
    }

    private void fillOutputTableWithFailureDetails(LinearLayout responseTable,
            Activity context, DiagnosticResponse resp) {
        createAndAddRowToOutput(context, responseTable, "neg. resp. code", 
                Utilities.getOutputTableResponseCodeOutput(resp), resp);
    }

    public LinearLayout getView() {
        return mView;
    }

    public VehicleMessage getRequest() {
        return mRequest;
    }

    public VehicleMessage getResponse() {
        return mResponse;
    }

}
