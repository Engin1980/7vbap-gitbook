import {SubmitHandler, useForm} from "react-hook-form";
import {MDBBtn, MDBContainer, MDBInput} from "mdb-react-ui-kit";
import axios from "axios";
import {toast} from "react-toastify";
import {useRef} from "react";
import Popup from "reactjs-popup";

type Data = {
  address : string;
  title : string;
  appUserId : number;
};

type Params = {
  appUserId : number;
  refresh : () => void;
};

function UrlEditPopup(params : Params){
  const {
    register,
    handleSubmit,
    formState : {errors}
  } = useForm<Data>();
  const cbRef = useRef<any>();

  const openPopup = () => cbRef.current.open();
  const closePopup = () => cbRef.current.close();

  const submitHandler : SubmitHandler<Data> = async data =>{
    const formData = new FormData();
    formData.append("address", data.address);
    formData.append("title", data.title);
    formData.append("appUserId", params.appUserId.toString());

    try{
      await axios.post("http://localhost:32123/v1/url", formData);
      closePopup();
      params.refresh();
      toast.success("Link stored successfully.");
    }catch (err){
      console.log(err);
      toast.error("Link stored failed.");
    }
  }

  return (<div>
    <MDBBtn onClick={openPopup}>Add URL</MDBBtn>
    <Popup ref={cbRef}>
      <MDBContainer>
            <h1>Edit link details</h1>
            <form onSubmit={handleSubmit(submitHandler)}>

              <MDBInput
                className="mb-3 mt-4"
                label="Title"
                type="text"
                {...register("title", {
                  required: "This field is required."
                })}
              />
              {errors.title && (
                <span className="invalid-feedback d-block">
                          {errors.title.message}
                        </span>
              )}

              <MDBInput
                className="mb-3 mt-4"
                label="Address"
                type="url"
                {...register("address", {
                  required: "This field is required",
                  pattern: {
                    value: /^http.+\..+/,
                    message: "URL Address must start with http...",
                  },
                })} />
              {errors.address && (
                <span className="invalid-feedback d-block">
                        {errors.address.message}
                      </span>
              )}

              <MDBBtn className="mt-2" type="submit">Save</MDBBtn>
            </form>
      </MDBContainer>
    </Popup>
  </div>);
}

export default UrlEditPopup;