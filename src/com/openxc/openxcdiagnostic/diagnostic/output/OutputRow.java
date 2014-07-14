package com.openxc.openxcdiagnostic.diagnostic.output;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openxc.messages.Command;
import com.openxc.messages.CommandResponse;
import com.openxc.messages.DiagnosticRequest;
import com.openxc.messages.DiagnosticResponse;
import com.openxc.messages.VehicleMessage;
import com.openxc.openxcdiagnostic.R;
import com.openxc.openxcdiagnostic.diagnostic.DiagnosticActivity;
import com.openxc.openxcdiagnostic.diagnostic.ResponseDetailsAlertManager;
import com.openxc.openxcdiagnostic.diagnostic.pair.CommandPair;
import com.openxc.openxcdiagnostic.diagnostic.pair.DiagnosticPair;
import com.openxc.openxcdiagnostic.diagnostic.pair.Pair;
import com.openxc.openxcdiagnostic.util.Formatter;
import com.openxc.openxcdiagnostic.util.Toaster;
import com.openxc.openxcdiagnostic.util.Utilities;

public class OutputRow {

    private LinearLayout mView;
    private OutputTableManager mTableManager;
    private VehicleMessage mResponse;
    private VehicleMessage mRequest;

    public OutputRow(DiagnosticActivity context,
            OutputTableManager tableManager, VehicleMessage req,
            VehicleMessage resp) {

        mView = (LinearLayout) context.getLayoutInflater().inflate(R.layout.diagoutputrow, null);
        mTableManager = tableManager;
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

        ((Button) mView.findViewById(R.id.outputMoreButton))
        .setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ResponseDetailsAlertManager.show(context, req, resp);
            }
        });

        ((Button) mView.findViewById(R.id.responseDeleteButton))
        .setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTableManager.removeRow(OutputRow.this);
            }
        });

        ((Button) mView.findViewById(R.id.outputResendButton))
        .setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String message;
                if (req instanceof DiagnosticRequest) {
                    message = "Resending Request.";
                    context.send(req);
                } else if (req instanceof Command) {
                    message = "Resending Command.";
                    context.send(req);
                } else {
                    message = "Cannot be resent...no request/command found.";
                }
                Toaster.showToast(context, message);
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
            valueText.setTextColor(Formatter.getOutputColor(context, (DiagnosticResponse) msg));
        }
        parent.addView(row);
    }

    private void fillOutputResponseTable(DiagnosticActivity context,
            final VehicleMessage msgResponse) {

        LinearLayout infoTable = (LinearLayout) mView.findViewById(R.id.outputInfo);
            if (msgResponse instanceof DiagnosticResponse) {
                DiagnosticResponse resp = (DiagnosticResponse) msgResponse;
                createAndAddRowToOutput(context, infoTable, "bus", Formatter.getBusOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "id", Formatter.getIdOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "mode", Formatter.getModeOutput(resp), resp);
                createAndAddRowToOutput(context, infoTable, "pid", Formatter.getPidOutput(resp), resp);
                boolean responseSuccess = resp.isSuccessful();
                createAndAddRowToOutput(context, infoTable, "success", Formatter.getSuccessOutput(resp), resp);
                if (responseSuccess) {
                    fillOutputTableWithSuccessDetails(infoTable, context, resp);
                } else {
                    fillOutputTableWithFailureDetails(infoTable, context, resp);
                }
            } else if (msgResponse instanceof CommandResponse ){
                CommandResponse cmdResponse = (CommandResponse) msgResponse;
                createAndAddRowToOutput(context, infoTable, cmdResponse.getCommand().toString(), 
                        Formatter.getMessageOutput(cmdResponse), cmdResponse);
            }
    }

    private void fillOutputTableWithSuccessDetails(LinearLayout responseTable,
            Activity context, DiagnosticResponse resp) {
        createAndAddRowToOutput(context, responseTable, "payload", Formatter.getPayloadOutput(resp), resp);
        createAndAddRowToOutput(context, responseTable, "value", Formatter.getValueOutput(resp), resp);
    }

    private void fillOutputTableWithFailureDetails(LinearLayout responseTable,
            Activity context, DiagnosticResponse resp) {
        createAndAddRowToOutput(context, responseTable, "neg. resp. code", 
                Formatter.getOutputTableResponseCodeOutput(resp), resp);
    }

    public LinearLayout getView() {
        return mView;
    }

    public Pair getPair() {
        if (mResponse instanceof DiagnosticResponse) {
            return new DiagnosticPair(mRequest == null ? null : (DiagnosticRequest) mRequest,
                    (DiagnosticResponse) mResponse);
        }
        return new CommandPair(mRequest == null ? null : (Command) mRequest, (CommandResponse) mResponse);
    }

}
