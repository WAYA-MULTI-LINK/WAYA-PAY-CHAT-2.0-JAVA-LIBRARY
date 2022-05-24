package com.faymosInc.wayapaylib.constants;

import lombok.*;

@Getter
@Setter
public class ResponseDto {
        private long timeStamp;
        public boolean status;
        public String message;
        public Data2 data;
}
