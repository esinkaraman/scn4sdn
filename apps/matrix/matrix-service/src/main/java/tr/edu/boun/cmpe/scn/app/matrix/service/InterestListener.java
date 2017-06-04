package tr.edu.boun.cmpe.scn.app.matrix.service;

import tr.edu.boun.cmpe.scn.api.common.ScnMessageType;
import tr.edu.boun.cmpe.scn.api.common.Tool;
import tr.edu.boun.cmpe.scn.api.message.Argument;
import tr.edu.boun.cmpe.scn.api.message.Arguments;
import tr.edu.boun.cmpe.scn.api.message.ServiceData;
import tr.edu.boun.cmpe.scn.api.message.ServiceInterest;
import tr.edu.boun.cmpe.scn.app.matrix.common.Constants;
import tr.edu.boun.cmpe.scn.service.worker.IServiceInterestListener;

import java.io.UnsupportedEncodingException;

/**
 * Created by esinka on 3/22/2017.
 */
public class InterestListener implements IServiceInterestListener {
    private static final String RESPONSE = "ok";

    @Override
    public ServiceData processInterest(ServiceInterest serviceInterest) {
        return prepareResponse(serviceInterest);
    }

    private ServiceData prepareResponse(ServiceInterest interest) {
        long start = System.nanoTime();
        ServiceData serviceData = new ServiceData();
        serviceData.setServiceName(interest.getServiceName());
        serviceData.setMessageTypeId(ScnMessageType.DATA.getId());
        serviceData.setMessageId(interest.getMessageId());

        Arguments arguments = interest.getArguments();
        int dimension = 0;
        if (arguments != null
                && arguments.getArgument() != null
                && !arguments.getArgument().isEmpty()) {

            for (Argument arg : arguments.getArgument()) {
                if (arg.getName() != null && arg.getName().trim().equals(Constants.ARG_KEY)) {
                    dimension = parseInt(arg.getValue());
                    break;
                }
            }
        }

        if (dimension > 0) {
            byte[][] multiplied = Multiplier.multiplyMatrices(dimension);
            //String matrixStr = matrixToString(multiplied, dimension);

            multiplied = null;
            //System.out.println("matrixStr:\n" + matrixStr);
            try {
                //serviceData.setData(matrixStr.getBytes(tr.edu.boun.cmpe.scn.api.common.Constants.UTF8));
                serviceData.setData(RESPONSE.getBytes(tr.edu.boun.cmpe.scn.api.common.Constants.UTF8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        /*System.out.println(Tool.formatTime() + " Sending ServiceData back. MsgID=" + interest.getMessageId() +
                                   " Elapsed=" + (System.nanoTime() - start) +
                                   " Dimension=" + dimension);*/
        return serviceData;
    }

    private int parseInt(String str) {
        int result = 0;
        if (str != null) {
            try {
                result = Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }

    private String matrixToString(byte[][] multiplied, int dimension) {
        StringBuilder bf = new StringBuilder();
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                bf.append(multiplied[i][j] + " ");
            }
            bf.append("\n");
        }
        return bf.toString();
    }
}